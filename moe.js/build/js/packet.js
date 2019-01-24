System.register(["./channels"], function (exports_1, context_1) {
    "use strict";
    var channels_1, WrappedPacket;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [
            function (channels_1_1) {
                channels_1 = channels_1_1;
            }
        ],
        execute: function () {
            WrappedPacket = class WrappedPacket {
                constructor(buffer) {
                    if (buffer instanceof ArrayBuffer)
                        this.buffer = buffer;
                    else if (typeof buffer === 'number') {
                        if (buffer < 0)
                            throw new RangeError("Data must be at least 0 bytes long");
                        this.buffer = new ArrayBuffer(12 + buffer);
                    }
                    else
                        this.buffer = new ArrayBuffer(16);
                    this.length = this.buffer.byteLength;
                    this.view = new DataView(this.buffer);
                }
                getLength() {
                    return this.length;
                }
                setLength(value) {
                    this.length = value;
                }
                getId() {
                    return this.view.getUint32(4);
                }
                setId(value) {
                    this.view.setUint32(4, value);
                }
                getAckId() {
                    return this.view.getUint32(8);
                }
                setAckId(value) {
                    return this.view.setUint32(8, value);
                }
                getChannelId() {
                    return this.view.getUint16(0);
                }
                setChannelId(value) {
                    this.view.setUint16(0, value);
                }
                getTypeCode() {
                    return this.view.getUint16(2);
                }
                getType() {
                    return channels_1.PacketTypeCode[this.getTypeCode()];
                }
                setTypeCode(value) {
                    this.view.setUint16(2, value);
                }
                getView() {
                    return this.view;
                }
                getDataView() {
                    return new DataView(this.getArrayBuffer(), 12);
                }
                getArrayBuffer() {
                    return this.buffer;
                }
            };
            exports_1("WrappedPacket", WrappedPacket);
        }
    };
});
//# sourceMappingURL=packet.js.map