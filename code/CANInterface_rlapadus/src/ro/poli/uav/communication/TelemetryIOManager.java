package ro.poli.uav.communication;

import ro.poli.uav.client.UAV;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 6/11/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TelemetryIOManager implements Runnable, TelemetryOfOtherUAVSubject, TelemetryOfOtherUAVObserver {
    private final int TELEMETRY_ROUTER_PORT = 4444;
    private int mavNumber;
    private UAV uav;
    private SocketChannel channel;
    private Selector selector;
    private PayloadBuffer payloadBuffer;
    private TelemetryOfOtherUAVObserver telemetryOfOtherUAVObserver;
    private String completePayloadMessage;
    private List<String> outMessages;

    public TelemetryIOManager(int mavNumber, UAV uav) throws RouterException {
        this.uav = uav;
        this.mavNumber = mavNumber;
        this.payloadBuffer = new PayloadBuffer();
        outMessages = new ArrayList<String>();
        initClient();
    }

    private void initClient() throws RouterException {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("localhost", TELEMETRY_ROUTER_PORT));

            waitWhileItConnects();
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (IOException e) {
            throw new RouterException("Connection error");
        }
    }

    private void waitWhileItConnects() throws RouterException {
        try {
            while (!channel.finishConnect()) {
            }
        } catch (IOException e) {
            throw new RouterException("Exception in finish connection");
        }
    }

    /**
     * @throws RouterException
     */
    public void listen() throws RouterException {
        for (; ; ) {

            try {
                if (selector.select() == 0)
                    continue;
            } catch (IOException e) {
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }

                iterator.remove();

            }
        }
    }

    /**
     * Reads data from an unblocking channel.
     *
     * @param key selection key
     * @throws RouterException
     */
    private void read(SelectionKey key) throws RouterException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        SocketChannel clientChannel = (SocketChannel) key.channel();
        int bytesRead;

        try {
            if ((bytesRead = clientChannel.read(buffer)) > 0) {
                buffer.flip();
                CharBuffer payloadPart = Charset.defaultCharset().decode(buffer);
                payloadBuffer.addPayloadPart(payloadPart);

                if (payloadBuffer.isPayloadMessageComplete()) {
                    completePayloadMessage = payloadBuffer.getCompletePayloadMessage();
                    System.out.println("Message from TR: " + completePayloadMessage);
                    notifyObserver();

                }
                buffer.clear();
            }

            if (bytesRead < 0) {
                clientChannel.close();
            }
        } catch (IOException e) {
            throw new RouterException("Can't read from client");
        }
    }

    /**
     * Writes data to an unblocking channel.
     *
     * @param key selection key
     * @throws RouterException
     */
    private void write(SelectionKey key) throws RouterException{
        Payload payload = new TelemetryPayload.Builder(mavNumber, uav.getLatitude(), uav.getLongitude(), uav.getAltitude())
                .heading(uav.getHeading()).speed(uav.getSpeed()).build();
        CharBuffer buffer = CharBuffer.wrap(payload.serialize());
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        while (buffer.hasRemaining()) {
            try {
                clientSocketChannel.write(Charset.defaultCharset().encode(buffer));
            } catch (IOException e) {
                throw new RouterException("Can't write to client");
            }
        }
        buffer.clear();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RouterException("Thread interrupted exception");
        }
       
        if (!outMessages.isEmpty())
        {
        	Payload p = new Payload(outMessages.get(outMessages.size() - 1));
            buffer = CharBuffer.wrap(p.serialize());
            while (buffer.hasRemaining()) {
                try {
                    clientSocketChannel.write(Charset.defaultCharset().encode(buffer));
                } catch (IOException e) {
                    throw new RouterException("Can't write to client");
                }
            }
            buffer.clear();
            outMessages.remove(outMessages.size() - 1);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RouterException("Thread interrupted exception");
            }
        }
    }

    @Override
    public void run() {
        try {
            listen();
        } catch (RouterException e) {
        }
    }

    @Override
    public void registerObserver(TelemetryOfOtherUAVObserver telemetryOfOtherUAVObserver) {
        this.telemetryOfOtherUAVObserver = telemetryOfOtherUAVObserver;
    }

    @Override
    public void notifyObserver() {
    	if (completePayloadMessage.substring(0, 3).equals("UAV"))
    	{
    		telemetryOfOtherUAVObserver.updateTelemetryOfOtherUAVs(completePayloadMessage);
    	}
    	else
    	{
    		telemetryOfOtherUAVObserver.updateMessagesFromOtherUAVs(completePayloadMessage);    		
    	}
    }

	@Override
	public void updateTelemetryOfOtherUAVs(String telemetryOfOther) {		
	}

	@Override
	public void updateMessagesFromOtherUAVs(String messageFromOther) {
		outMessages.add(messageFromOther);
	}
}
