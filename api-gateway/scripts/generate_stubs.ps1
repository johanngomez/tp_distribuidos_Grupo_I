Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Set-Location (Join-Path $PSScriptRoot "..")
& .\.venv\Scripts\python.exe -m grpc_tools.protoc `
  -I proto `
  --python_out=app/generated `
  --grpc_python_out=app/generated `
  proto/vehicle.proto

if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}
