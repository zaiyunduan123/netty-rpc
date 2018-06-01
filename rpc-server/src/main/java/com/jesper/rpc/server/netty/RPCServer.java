package com.jesper.rpc.server.netty;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 服务器端，通过SocketServer，持续接收客户端的请求，并将客户端的请求分发到指定的处理器出去处理。
 */
public class RPCServer implements InitializingBean, Lifecycle, ApplicationContextAware {

    //服务端口号
    private int port = 12000;

    private ServerSocket server;
    //线程池
    private Executor executorService;

    public Map<String, Object> handlderMap = new ConcurrentHashMap<>();

    private void publishService() throws Exception {
        server = new ServerSocket();
        for (; ; ) {
            try {
                final Socket socket = server.accept();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                            // 获取引用
                            String interfaceName = input.readUTF();

                            //获取方法名
                            String methodName = input.readUTF();

                            Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                            Object[] arguments = (Object[]) input.readObject();

                            try {
                                Object service = handlderMap.get(interfaceName);
                                Method method = service.getClass().getMethod(methodName, parameterTypes);
                                Object result = method.invoke(service, arguments);
                            } catch (Throwable t) {
                                output.writeObject(t);
                            } finally {
                                input.close();
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
         //发布服务
        publishService();
    }

    public void setExecutorService(Executor executorService){
        this.executorService = executorService;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
