System.register([], function (exports_1, context_1) {
    "use strict";
    var DataChannelDirection, DataChannelMediaType, PacketTypeCode, PacketRecievedEvent;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [],
        execute: function () {
            (function (DataChannelDirection) {
                DataChannelDirection[DataChannelDirection["DEFAULT"] = 0] = "DEFAULT";
                DataChannelDirection[DataChannelDirection["SERVER_TO_CLIENT"] = 1] = "SERVER_TO_CLIENT";
                DataChannelDirection[DataChannelDirection["CLIENT_TO_SERVER"] = 2] = "CLIENT_TO_SERVER";
                DataChannelDirection[DataChannelDirection["BOTH"] = 3] = "BOTH";
            })(DataChannelDirection || (DataChannelDirection = {}));
            exports_1("DataChannelDirection", DataChannelDirection);
            (function (DataChannelMediaType) {
                DataChannelMediaType[DataChannelMediaType["META"] = 0] = "META";
                DataChannelMediaType[DataChannelMediaType["AUDIO"] = 1] = "AUDIO";
                DataChannelMediaType[DataChannelMediaType["VIDEO"] = 2] = "VIDEO";
                DataChannelMediaType[DataChannelMediaType["AUDIO_VIDEO"] = 3] = "AUDIO_VIDEO";
                DataChannelMediaType[DataChannelMediaType["PROPERTY_ACCESS"] = 4] = "PROPERTY_ACCESS";
                DataChannelMediaType[DataChannelMediaType["TEXT_STREAM"] = 5] = "TEXT_STREAM";
                DataChannelMediaType[DataChannelMediaType["OBJECT_STREAM"] = 6] = "OBJECT_STREAM";
            })(DataChannelMediaType || (DataChannelMediaType = {}));
            exports_1("DataChannelMediaType", DataChannelMediaType);
            (function (PacketTypeCode) {
                PacketTypeCode[PacketTypeCode["SERVER_HELLO"] = 0] = "SERVER_HELLO";
                PacketTypeCode[PacketTypeCode["CLIENT_HELLO"] = 1] = "CLIENT_HELLO";
                PacketTypeCode[PacketTypeCode["ERROR"] = 2] = "ERROR";
                PacketTypeCode[PacketTypeCode["ACK"] = 3] = "ACK";
                PacketTypeCode[PacketTypeCode["CHANNEL_ENUMERATION_REQUEST"] = 4] = "CHANNEL_ENUMERATION_REQUEST";
                PacketTypeCode[PacketTypeCode["CHANNEL_ENUMERATION"] = 5] = "CHANNEL_ENUMERATION";
                PacketTypeCode[PacketTypeCode["CHANNEL_SUBSCRIBE"] = 6] = "CHANNEL_SUBSCRIBE";
                PacketTypeCode[PacketTypeCode["CHANNEL_UNSUBSCRIBE"] = 7] = "CHANNEL_UNSUBSCRIBE";
                PacketTypeCode[PacketTypeCode["CHANNEL_CLOSE"] = 8] = "CHANNEL_CLOSE";
                PacketTypeCode[PacketTypeCode["CHANNEL_METADATA_REQUEST"] = 9] = "CHANNEL_METADATA_REQUEST";
                PacketTypeCode[PacketTypeCode["CHANNEL_METADATA"] = 10] = "CHANNEL_METADATA";
                PacketTypeCode[PacketTypeCode["PROPERTY_ENUMERATION_REQUEST"] = 11] = "PROPERTY_ENUMERATION_REQUEST";
                PacketTypeCode[PacketTypeCode["PROPERTY_ENUMERATION"] = 12] = "PROPERTY_ENUMERATION";
                PacketTypeCode[PacketTypeCode["PROPERTY_VALUES_REQUEST"] = 13] = "PROPERTY_VALUES_REQUEST";
                PacketTypeCode[PacketTypeCode["PROPERTY_VALUES"] = 14] = "PROPERTY_VALUES";
                PacketTypeCode[PacketTypeCode["PROPERTY_VALUE_SET"] = 15] = "PROPERTY_VALUE_SET";
                PacketTypeCode[PacketTypeCode["STREAM_META"] = 16] = "STREAM_META";
                PacketTypeCode[PacketTypeCode["STREAM_FRAME"] = 17] = "STREAM_FRAME";
            })(PacketTypeCode || (PacketTypeCode = {}));
            exports_1("PacketTypeCode", PacketTypeCode);
            PacketRecievedEvent = class PacketRecievedEvent extends Event {
                constructor(packet) {
                    super('packet', { cancelable: false });
                    this.packet = packet;
                }
            };
            exports_1("PacketRecievedEvent", PacketRecievedEvent);
        }
    };
});
//# sourceMappingURL=channels.js.map