Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Set-Location (Join-Path $PSScriptRoot "..")
foreach ($contract in @(
  "rentar/customer/v1/customer.proto",
  "rentar/vehicle/v1/vehicle.proto",
  "rentar/rental/v1/rental.proto"
)) {
  & .\.venv\Scripts\python.exe -m grpc_tools.protoc `
    -I proto `
    --python_out=app/generated `
    --grpc_python_out=app/generated `
    (Join-Path "proto" $contract)

  if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
  }
}

# grpcio-tools genera imports absolutos; al mantener los stubs dentro de
# app.generated deben ser imports relativos para que el Gateway los encuentre.
foreach ($stub in @(
  "app/generated/rentar/customer/v1/customer_pb2_grpc.py",
  "app/generated/rentar/vehicle/v1/vehicle_pb2_grpc.py",
  "app/generated/rentar/rental/v1/rental_pb2_grpc.py"
)) {
  $path = $stub
  (Get-Content -Raw $path) `
    -replace "from rentar\.customer\.v1 import customer_pb2 as rentar_dot_customer_dot_v1_dot_customer__pb2", "from . import customer_pb2 as rentar_dot_customer_dot_v1_dot_customer__pb2" `
    -replace "from rentar\.vehicle\.v1 import vehicle_pb2 as rentar_dot_vehicle_dot_v1_dot_vehicle__pb2", "from . import vehicle_pb2 as rentar_dot_vehicle_dot_v1_dot_vehicle__pb2" `
    -replace "from rentar\.rental\.v1 import rental_pb2 as rentar_dot_rental_dot_v1_dot_rental__pb2", "from . import rental_pb2 as rentar_dot_rental_dot_v1_dot_rental__pb2" |
    Set-Content -Path $path -NoNewline
}

if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}
