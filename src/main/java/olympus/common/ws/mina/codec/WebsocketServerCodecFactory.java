package olympus.common.ws.mina.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class WebsocketServerCodecFactory implements ProtocolCodecFactory {

    public static final String WEBSOCKET_STATE_ATT = "ws.state";
    public static final String WEBSOCKET_HANDLER_ATT = "ws.handler";

    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;

    public WebsocketServerCodecFactory() {
        encoder = new WebsocketServerEncoder();
        decoder = new WebsocketServerDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
