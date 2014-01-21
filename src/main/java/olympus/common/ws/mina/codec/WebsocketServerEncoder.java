package olympus.common.ws.mina.codec;

import olympus.common.ws.core.DecoderState;
import olympus.common.ws.core.WebsocketMessage;
import olympus.common.ws.core.WebsocketOpcode;
import olympus.common.ws.core.handlers.AbstractWebsocketHandler;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.ByteBuffer;

import static olympus.common.ws.mina.codec.WebsocketServerCodecFactory.WEBSOCKET_HANDLER_ATT;
import static olympus.common.ws.mina.codec.WebsocketServerCodecFactory.WEBSOCKET_STATE_ATT;

public class WebsocketServerEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws IllegalArgumentException {
        byte[] buffer;
        boolean isBinary = false;
        WebsocketOpcode opcode = WebsocketOpcode.TEXT;
        if (message instanceof String) {
            buffer = ((String) message).getBytes();
        } else if (message instanceof byte[]) {
            buffer = (byte[]) message;
            opcode = WebsocketOpcode.BINARY;
            isBinary = true;
        } else if (message instanceof IoBuffer) {
            buffer = ((IoBuffer) message).array();
            opcode = WebsocketOpcode.BINARY;
            isBinary = true;
        } else if (message instanceof WebsocketMessage) {
            WebsocketMessage websocketMessage = (WebsocketMessage) message;
            opcode = websocketMessage.getOpcode();
            buffer = websocketMessage.getPayload();
            isBinary = websocketMessage.isBinary();
        } else {
            throw new IllegalArgumentException("Only accepts byte[], String and IoBuffer");
        }
        DecoderState state = (DecoderState) session.getAttribute(WEBSOCKET_STATE_ATT);
        if (null == state) {
            session.setAttribute(WEBSOCKET_STATE_ATT, state = DecoderState.PROXY);
        }
        switch (state) {
            case HANDSHAKE:
                out.write(IoBuffer.wrap(buffer));
                break;
            case ACCEPTING_MESSAGES:
                AbstractWebsocketHandler websocketHandler = (AbstractWebsocketHandler) session.getAttribute(WEBSOCKET_HANDLER_ATT);
                ByteBuffer byteBuffer = websocketHandler.encodeMessage(opcode, buffer, isBinary);
                if(byteBuffer != null && byteBuffer.limit() != 0)
                    out.write(IoBuffer.wrap(byteBuffer));
                break;
            case PROXY:
                out.write(message);
                break;
        }

    }

    @Override
    public void dispose(IoSession session) throws Exception {

    }
}
