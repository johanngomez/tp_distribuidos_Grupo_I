package ar.unla.rentar.grpc.vehicle.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: vehicle.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class VehicleServiceGrpc {

  private VehicleServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "rentar.vehicle.v1.VehicleService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest,
      ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> getListVehiclesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListVehicles",
      requestType = ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest.class,
      responseType = ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest,
      ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> getListVehiclesMethod() {
    io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest, ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> getListVehiclesMethod;
    if ((getListVehiclesMethod = VehicleServiceGrpc.getListVehiclesMethod) == null) {
      synchronized (VehicleServiceGrpc.class) {
        if ((getListVehiclesMethod = VehicleServiceGrpc.getListVehiclesMethod) == null) {
          VehicleServiceGrpc.getListVehiclesMethod = getListVehiclesMethod =
              io.grpc.MethodDescriptor.<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest, ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListVehicles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new VehicleServiceMethodDescriptorSupplier("ListVehicles"))
              .build();
        }
      }
    }
    return getListVehiclesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest,
      ar.unla.rentar.grpc.vehicle.v1.Vehicle> getCreateVehicleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateVehicle",
      requestType = ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest.class,
      responseType = ar.unla.rentar.grpc.vehicle.v1.Vehicle.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest,
      ar.unla.rentar.grpc.vehicle.v1.Vehicle> getCreateVehicleMethod() {
    io.grpc.MethodDescriptor<ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest, ar.unla.rentar.grpc.vehicle.v1.Vehicle> getCreateVehicleMethod;
    if ((getCreateVehicleMethod = VehicleServiceGrpc.getCreateVehicleMethod) == null) {
      synchronized (VehicleServiceGrpc.class) {
        if ((getCreateVehicleMethod = VehicleServiceGrpc.getCreateVehicleMethod) == null) {
          VehicleServiceGrpc.getCreateVehicleMethod = getCreateVehicleMethod =
              io.grpc.MethodDescriptor.<ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest, ar.unla.rentar.grpc.vehicle.v1.Vehicle>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateVehicle"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ar.unla.rentar.grpc.vehicle.v1.Vehicle.getDefaultInstance()))
              .setSchemaDescriptor(new VehicleServiceMethodDescriptorSupplier("CreateVehicle"))
              .build();
        }
      }
    }
    return getCreateVehicleMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static VehicleServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VehicleServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VehicleServiceStub>() {
        @java.lang.Override
        public VehicleServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VehicleServiceStub(channel, callOptions);
        }
      };
    return VehicleServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static VehicleServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VehicleServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VehicleServiceBlockingStub>() {
        @java.lang.Override
        public VehicleServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VehicleServiceBlockingStub(channel, callOptions);
        }
      };
    return VehicleServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static VehicleServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<VehicleServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<VehicleServiceFutureStub>() {
        @java.lang.Override
        public VehicleServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new VehicleServiceFutureStub(channel, callOptions);
        }
      };
    return VehicleServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void listVehicles(ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest request,
        io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListVehiclesMethod(), responseObserver);
    }

    /**
     */
    default void createVehicle(ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest request,
        io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehicle> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateVehicleMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service VehicleService.
   */
  public static abstract class VehicleServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return VehicleServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service VehicleService.
   */
  public static final class VehicleServiceStub
      extends io.grpc.stub.AbstractAsyncStub<VehicleServiceStub> {
    private VehicleServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VehicleServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VehicleServiceStub(channel, callOptions);
    }

    /**
     */
    public void listVehicles(ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest request,
        io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListVehiclesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createVehicle(ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest request,
        io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehicle> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateVehicleMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service VehicleService.
   */
  public static final class VehicleServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<VehicleServiceBlockingStub> {
    private VehicleServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VehicleServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VehicleServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse listVehicles(ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListVehiclesMethod(), getCallOptions(), request);
    }

    /**
     */
    public ar.unla.rentar.grpc.vehicle.v1.Vehicle createVehicle(ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateVehicleMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service VehicleService.
   */
  public static final class VehicleServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<VehicleServiceFutureStub> {
    private VehicleServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VehicleServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new VehicleServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse> listVehicles(
        ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListVehiclesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ar.unla.rentar.grpc.vehicle.v1.Vehicle> createVehicle(
        ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateVehicleMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_VEHICLES = 0;
  private static final int METHODID_CREATE_VEHICLE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_VEHICLES:
          serviceImpl.listVehicles((ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest) request,
              (io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse>) responseObserver);
          break;
        case METHODID_CREATE_VEHICLE:
          serviceImpl.createVehicle((ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest) request,
              (io.grpc.stub.StreamObserver<ar.unla.rentar.grpc.vehicle.v1.Vehicle>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListVehiclesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ar.unla.rentar.grpc.vehicle.v1.ListVehiclesRequest,
              ar.unla.rentar.grpc.vehicle.v1.ListVehiclesResponse>(
                service, METHODID_LIST_VEHICLES)))
        .addMethod(
          getCreateVehicleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ar.unla.rentar.grpc.vehicle.v1.CreateVehicleRequest,
              ar.unla.rentar.grpc.vehicle.v1.Vehicle>(
                service, METHODID_CREATE_VEHICLE)))
        .build();
  }

  private static abstract class VehicleServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    VehicleServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ar.unla.rentar.grpc.vehicle.v1.VehicleProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("VehicleService");
    }
  }

  private static final class VehicleServiceFileDescriptorSupplier
      extends VehicleServiceBaseDescriptorSupplier {
    VehicleServiceFileDescriptorSupplier() {}
  }

  private static final class VehicleServiceMethodDescriptorSupplier
      extends VehicleServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    VehicleServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (VehicleServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new VehicleServiceFileDescriptorSupplier())
              .addMethod(getListVehiclesMethod())
              .addMethod(getCreateVehicleMethod())
              .build();
        }
      }
    }
    return result;
  }
}
