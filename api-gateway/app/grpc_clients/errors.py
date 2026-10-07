from dataclasses import dataclass

import grpc


@dataclass(frozen=True)
class GrpcServiceError(Exception):
    code: grpc.StatusCode
    details: str
