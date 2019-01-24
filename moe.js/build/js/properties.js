System.register(["./packet"], function (exports_1, context_1) {
    "use strict";
    var packet_1, Property, PropertyList, packet, view, i, id, type, name, prop, Property;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [
            function (packet_1_1) {
                packet_1 = packet_1_1;
            }
        ],
        execute: function () {
            Property = class Property {
            };
            exports_1("Property", Property);
            ;
            PropertyList = class PropertyList {
                constructor(channel) {
                    this.channel = channel;
                }
            };
            exports_1("PropertyList", PropertyList);
             > getProperties();
            {
                packet = new packet_1.WrappedPacket(0);
                packet.setTypeCode(PacketTypeCode.PROPERTY_ENUMERATION_REQUEST);
                return this.channel.sendPacket(packet, true)
                    .then(ack -  > {
                    var: data = ack.getDataView(),
                    var: len, number = data.getUint16(),
                    const: decoder = new TextDecoder("utf8"),
                    var: pos, number = 2,
                    function: readNext()
                }, {
                    var: len, number = data.getUint8(pos++),
                    if(len) { }
                } == 0xFF);
                {
                    len += data.getUint16(pos);
                    pos += 2;
                }
                view = new DataView(data.buffer, data.byteOffset + pos, len);
                pos += len;
                return decoder.decode(view);
            }
            for (i = 0; i < len; i++) {
                id = data.getUint16(pos);
                pos += 2;
                type = data.getUint8(pos);
                pos++;
                name = readNext();
                if (id in this.properties)
                    prop = this.properties[id];
                else
                    prop = this.properties[id] = new Property(this, id);
                prop._type = type;
                prop._name = name;
            }
            return this.properties;
            ;
            update(prop, Property);
            Promise < Property > {
                var: packet = new packet_1.WrappedPacket(4),
                packet: .setTypeCode(PacketTypeCode.PROPERTY_VALUES_REQUEST),
                packet: .getDataView()
                    .setUint16(1)
                    .setUint16(prop.id),
                return(, Promise, , DataPacket) { }
            } >> this.channel.sendPacket(packet, true);
            then(ack -  > {
                return: prop
            });
            set(prop, Property, value, number);
            Promise < Property > {
                return: null
            };
            Property = class Property {
                constructor(list, id) {
                    this.cached = false;
                    this.list = list;
                    this.id = id;
                }
                get min() {
                    return this._min;
                }
                get max() {
                    return this._max;
                }
                get step() {
                    return this._step;
                }
                get values() {
                    return this._values;
                }
                getValue(cached = false) {
                    if (cached && this.cached)
                        return Promise.resolved(this._value);
                    return list.update(this)
                        .then(v -  > (this._value));
                }
                setValue(value) {
                    var iValue;
                    if (value instanceof string)
                        iValue = this._values.indexOf(value);
                    else
                        iValue = value;
                    this._value = iValue;
                    return list.set(this, iValue);
                }
            };
        }
    };
});
//# sourceMappingURL=properties.js.map