import { useState, useCallback, useRef } from "react";

// Hook reutilizable para mostrar un mensaje de éxito que desaparece
// solo después de unos segundos (por defecto 3000ms)
export function useMensajeExito(duracionMs = 3000) {
    const [mensaje, setMensaje] = useState("");
    const timeoutRef = useRef(null);

    const mostrarMensaje = useCallback(
        (texto) => {
            setMensaje(texto);

            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }

            timeoutRef.current = setTimeout(() => {
                setMensaje("");
            }, duracionMs);
        },
        [duracionMs]
    );

    return [mensaje, mostrarMensaje];
}
