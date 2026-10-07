import base64
import hashlib
import hmac
import json
import socket
import threading
import time
import unittest
from dataclasses import replace
from urllib.error import HTTPError
from urllib.request import Request, urlopen

from fastapi import HTTPException

from app.main import app
from app.security.dependencies import require_admin
from app.security.jwt import AuthenticatedUser


# Clave ficticia: nunca usar credenciales reales en estas pruebas.
TEST_SECRET = "clave-de-pruebas-jwt-rentar-32-bytes-minimo"


def make_token(changes=None, omit=(), secret=TEST_SECRET, algorithm="HS256"):
    """Firma independiente de PyJWT con el formato observado en el emisor Java."""
    now = int(time.time())
    claims = {
        "iss": "rentar", "sub": "cliente@example.com", "iat": now,
        "exp": now + 7200, "esAdmin": False,
    }
    claims.update(changes or {})
    for field in omit:
        claims.pop(field)

    def encode(value):
        return base64.urlsafe_b64encode(value).rstrip(b"=")

    header = {"alg": algorithm, "typ": "JWT"}
    payload = b".".join(encode(json.dumps(value).encode()) for value in (header, claims))
    digest = hashlib.sha256 if algorithm == "HS256" else hashlib.sha384
    signature = hmac.new(secret.encode("utf-8"), payload, digest).digest()
    return (payload + b"." + encode(signature)).decode()


class SecurityTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.original_settings = app.state.settings
        app.state.settings = replace(cls.original_settings, jwt_secret=TEST_SECRET)
        import uvicorn

        cls.sock = socket.socket()
        cls.sock.bind(("127.0.0.1", 0))
        cls.url = f"http://127.0.0.1:{cls.sock.getsockname()[1]}"
        cls.server = uvicorn.Server(uvicorn.Config(app, log_level="error"))
        cls.thread = threading.Thread(
            target=cls.server.run, kwargs={"sockets": [cls.sock]}, daemon=True,
        )
        cls.thread.start()
        for _ in range(100):
            if cls.server.started:
                break
            time.sleep(0.05)
        if not cls.server.started:
            cls.tearDownClass()
            raise RuntimeError("No inició el servidor de prueba")

    @classmethod
    def tearDownClass(cls):
        cls.server.should_exit = True
        cls.thread.join(timeout=5)
        cls.sock.close()
        app.state.settings = cls.original_settings

    def request(self, path="/auth/me", authorization=None):
        headers = {} if authorization is None else {"Authorization": authorization}
        try:
            response = urlopen(Request(self.url + path, headers=headers), timeout=5)
        except HTTPError as error:
            response = error
        with response:
            return response.status, response.headers, json.load(response)

    def test_valid_client_and_admin(self):
        for role in (False, True):
            with self.subTest(role=role):
                code, _, body = self.request(authorization="Bearer " + make_token({"esAdmin": role}))
                self.assertEqual(code, 200)
                self.assertEqual(body, {"email": "cliente@example.com", "esAdmin": role})

    def test_invalid_credentials(self):
        tokens = [
            None, "Basic abc", "Bearer", "Bearer no-es-un-jwt",
            "Bearer " + make_token(secret="otra-clave-de-pruebas-de-al-menos-32-bytes"),
            "Bearer " + make_token({"exp": int(time.time()) - 10}),
            "Bearer " + make_token({"iss": "otro-emisor"}),
            "Bearer " + make_token({"iat": int(time.time()) + 3600}),
            "Bearer " + make_token({"sub": ""}),
            "Bearer " + make_token({"esAdmin": "false"}),
            "Bearer " + make_token({"esAdmin": 1}),
            "Bearer " + make_token(algorithm="HS384"),
        ]
        tokens.extend("Bearer " + make_token(omit=[name]) for name in ["iss", "sub", "iat", "exp", "esAdmin"])
        for index, token in enumerate(tokens):
            with self.subTest(case=index):
                code, headers, _ = self.request(authorization=token)
                self.assertEqual(code, 401)
                self.assertEqual(headers["WWW-Authenticate"], "Bearer")

    def test_missing_secret_does_not_bypass_security(self):
        configured = app.state.settings
        app.state.settings = replace(configured, jwt_secret=None)
        try:
            code, _, _ = self.request(authorization="Bearer " + make_token())
            self.assertEqual(code, 503)
            self.assertEqual(self.request("/health")[0], 200)
        finally:
            app.state.settings = configured

    def test_health_is_public(self):
        code, _, body = self.request("/health")
        self.assertEqual(code, 200)
        self.assertEqual(body, {"status": "ok"})

    def test_admin_permission(self):
        admin = AuthenticatedUser(email="admin@example.com", es_admin=True)
        self.assertEqual(require_admin(admin), admin)
        with self.assertRaises(HTTPException) as caught:
            require_admin(AuthenticatedUser(email="cliente@example.com", es_admin=False))
        self.assertEqual(caught.exception.status_code, 403)

    def test_secret_is_not_printed_with_settings(self):
        self.assertNotIn(TEST_SECRET, repr(app.state.settings))


if __name__ == "__main__":
    unittest.main()
