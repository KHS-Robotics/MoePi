System.register(["./events", "./channels", "./packet"], function (exports_1, context_1) {
    "use strict";
    var events_1, channels_1, packet_1, WebsocketDataStream, WebsocketDataChannel;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [
            function (events_1_1) {
                events_1 = events_1_1;
            },
            function (channels_1_1) {
                channels_1 = channels_1_1;
            },
            function (packet_1_1) {
                packet_1 = packet_1_1;
            }
        ],
        execute: function () {
            WebsocketDataStream = class WebsocketDataStream extends events_1.Emitter {
                constructor(socket) {
                    super();
                    this.ackHandlers = {};
                    this.channels = {};
                    this.lastId = 1;
                    this.socket = socket;
                    this.socket.binaryType = 'arraybuffer';
                    this.socket.addEventListener('message', packet => this._recvPacket(new packet_1.WrappedPacket(packet.data)));
                    this.channels[0] = new WebsocketDataChannel(this, {
                        id: 0,
                        name: "META",
                        mediaType: channels_1.DataChannelMediaType.META,
                        direction: channels_1.DataChannelDirection.BOTH
                    });
                }
                static connect(options) {
                    var socket = new WebSocket(options.url, options.protocol || options.protocols);
                    return new Promise((y, n) => {
                        var h1 = e => { socket.removeEventListener('error', h2); y(e); };
                        var h2 = e => { socket.removeEventListener('open', h1); n(e); };
                        socket.addEventListener('open', h1, { once: true });
                        socket.addEventListener('error', h2, { once: true });
                        if (socket.readyState == WebSocket.OPEN)
                            socket.dispatchEvent(new Event('open'));
                        else if (socket.readyState != WebSocket.CONNECTING)
                            socket.dispatchEvent(new Event('error'));
                    }).then(e => (new WebsocketDataStream(socket)));
                }
                getAvailableChannels() {
                    var packet = new packet_1.WrappedPacket(0);
                    packet.setTypeCode(channels_1.PacketTypeCode.CHANNEL_ENUMERATION_REQUEST);
                    return this.channels[0].sendPacket(packet, true)
                        .then(p2 => {
                        var buf = p2.getDataView();
                        var numChannels = buf.getUint16(0);
                        var pos = 2;
                        for (var i = 0; i < numChannels; i++) {
                            var chIdv = buf.getUint16(pos);
                            pos += 2;
                            var chTyp = buf.getUint8(pos++);
                            var chDir = buf.getUint8(pos++);
                            if (chIdv in this.channels) {
                                var current = this.channels[chIdv];
                                if (current.getDirection() == chDir && current.getMediaType() == chTyp)
                                    continue;
                                if (current.isSubscribed())
                                    current.unsubscribe();
                            }
                            var channel = new WebsocketDataChannel(this, { id: chIdv, name: '???', direction: chIdv, mediaType: chTyp });
                            this.channels[chIdv] = channel;
                        }
                        return this.channels;
                    });
                }
                getChannel(index) {
                    return this.channels[index] || null;
                }
                _recvPacket(packet) {
                    console.log('received packet', packet, packet.getType());
                    if (packet.getAckId() in this.ackHandlers) {
                        var handler = this.ackHandlers[packet.getAckId()];
                        if (packet.getTypeCode() == channels_1.PacketTypeCode.ERROR)
                            handler.error(packet);
                        else
                            handler.success(packet);
                        delete this.ackHandlers[packet.getAckId()];
                    }
                    if (packet.getChannelId() in this.channels) {
                        var channel = this.channels[packet.getChannelId()];
                        channel.dispatchEvent(new channels_1.PacketRecievedEvent(packet));
                    }
                    else {
                    }
                }
                _sendPacket(packet, expectResponse = false) {
                    packet.setId(++this.lastId);
                    var result = undefined;
                    if (!this.isConnected()) {
                        if (expectResponse)
                            return Promise.reject('Connection is closed');
                        return;
                    }
                    if (expectResponse)
                        result = new Promise((yay, nay) => (this.ackHandlers[packet.getId()] = { success: yay, error: nay }));
                    this.socket.send(packet.getArrayBuffer());
                    return result;
                }
                isConnected() {
                    return this.socket.readyState == WebSocket.OPEN;
                }
                close() {
                    this.socket.close();
                }
            };
            exports_1("WebsocketDataStream", WebsocketDataStream);
            WebsocketDataChannel = class WebsocketDataChannel extends events_1.Emitter {
                constructor(stream, options) {
                    super();
                    this.subscribed = false;
                    this.stream = stream;
                    this.id = options.id;
                    this.name = options.name;
                    this.direction = options.direction;
                    this.mediaType = options.mediaType;
                }
                getId() {
                    return this.id;
                }
                getName() {
                    return this.name;
                }
                getDirection() {
                    return this.direction;
                }
                getMediaType() {
                    return this.mediaType;
                }
                subscribe() {
                    if (this.isSubscribed())
                        return Promise.resolve(this);
                    var packet = new packet_1.WrappedPacket(4);
                    packet.setTypeCode(channels_1.PacketTypeCode.CHANNEL_SUBSCRIBE);
                    var view = packet.getDataView();
                    view.setUint16(0, 1);
                    view.setUint16(2, this.getId());
                    return this.stream.channels[0].sendPacket(packet, true)
                        .then(ack => {
                        this.subscribed = true;
                        return this;
                    });
                }
                isSubscribed() {
                    return this.subscribed;
                }
                getMetadata() {
                    var reqPacket = new packet_1.WrappedPacket(2);
                    reqPacket.setTypeCode(channels_1.PacketTypeCode.CHANNEL_METADATA_REQUEST);
                    reqPacket.getDataView().setUint16(0, this.getId());
                    return this.stream.channels[0].sendPacket(reqPacket, true)
                        .then((packet) => {
                        const data = packet.getDataView();
                        const numEntries = data.getUint16(0);
                        const decoder = new TextDecoder("utf8");
                        var pos = 2;
                        function readNext() {
                            var len = data.getUint8(pos++);
                            if (len == 0xFF) {
                                len += data.getUint16(pos);
                                pos += 2;
                            }
                            var view = new DataView(data.buffer, data.byteOffset + pos, len);
                            pos += len;
                            return decoder.decode(view);
                        }
                        var result = {};
                        for (var i = 0; i < numEntries; i++) {
                            var key = readNext();
                            var value = readNext();
                            var obj = result;
                            var split;
                            while ((split = key.indexOf('.')) > 0) {
                                var tmp = key.substr(0, split);
                                key = key.substr(split + 1);
                                obj = obj[tmp] = obj[tmp] || {};
                            }
                            obj[key] = value;
                        }
                        return result;
                    });
                }
                sendPacket(packet, expectResponse = false) {
                    if (packet.getChannelId() != this.getId())
                        packet.setChannelId(this.getId());
                    return this.stream._sendPacket(packet, expectResponse);
                }
                unsubscribe() {
                    if (!this.isSubscribed()) {
                        this.handlers = {};
                        return Promise.resolve(this);
                    }
                    var unsubPacket = new packet_1.WrappedPacket(4);
                    unsubPacket.setTypeCode(channels_1.PacketTypeCode.CHANNEL_UNSUBSCRIBE);
                    var view = unsubPacket.getDataView();
                    view.setUint16(0, 1);
                    view.setUint16(2, this.getId());
                    return this.stream.channels[0].sendPacket(unsubPacket, true)
                        .then(ack => {
                        this.handlers = {};
                        this.subscribed = false;
                        return this;
                    });
                }
            };
        }
    };
});
//# sourceMappingURL=websock.js.map