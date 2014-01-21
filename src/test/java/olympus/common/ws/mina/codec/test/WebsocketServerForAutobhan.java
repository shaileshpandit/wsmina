package olympus.common.ws.mina.codec.test;

import olympus.common.ws.core.WebsocketMessage;
import olympus.common.ws.mina.codec.WebsocketServerCodecFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebsocketServerForAutobhan {

    public static final Logger logger = LoggerFactory.getLogger(WebsocketServerForAutobhan.class);
    private static final ExecutorFilter executorFilter = new ExecutorFilter(1);
    private static final LoggingFilter loggingFilter = new LoggingFilter();

    private static IoAcceptor bindWebSocketConnectionAcceptor(int port) throws IOException {
        SocketAcceptor acceptor = new NioSocketAcceptor();
        MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter(MdcInjectionFilter.MdcKey.remoteAddress);
        acceptor.getFilterChain().addLast("mdc", mdcInjectionFilter);
        acceptor.getFilterChain().addLast("ws", new ProtocolCodecFilter(new WebsocketServerCodecFactory()));
        acceptor.getFilterChain().addLast("executor", executorFilter);
        acceptor.getFilterChain().addLast("mdcafterexecutor", mdcInjectionFilter);
        acceptor.getFilterChain().addLast("logger", loggingFilter);

        acceptor.setReuseAddress(true);
        acceptor.setHandler(new IoHandlerAdapter() {
            public void messageReceived(IoSession session, Object message) throws Exception {
                if (message instanceof byte[]) {
                    logger.debug("got binary message :: {}", message);
                    session.write(message);
                } else if (message instanceof String) {
                    logger.debug("got text message :: {}", message);
                    session.write(message);
                } else if (message instanceof WebsocketMessage) {
                    logger.debug("got WebsocketMessage :: {}", message);
                }
            }
        });
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 300);
        acceptor.bind(new InetSocketAddress(port));

        return acceptor;
    }

    private static void addShutDownHook(final IoAcceptor acceptor) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    disposeAcceptors(true, acceptor);
                } catch (Throwable t) {
                    logger.error("Error while shutting down", t);
                }
            }
        }));
    }

    private static void disposeAcceptors(boolean awaitTermination, IoAcceptor... acceptors) {
        logger.info("unbind and disposing all acceptors - started");

        for (IoAcceptor acceptor : acceptors) {
            if (null != acceptor) {
                acceptor.unbind();
                acceptor.dispose(awaitTermination);
            }
        }

        logger.info("unbind and disposing all acceptors - done");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || null == args[0] || args[0].isEmpty()) {
            logger.debug("Usage : WebsocketServerForAutobhan <port>");
            System.exit(-1);
        }

        addShutDownHook(bindWebSocketConnectionAcceptor(Integer.parseInt(args[0])));
    }
}
