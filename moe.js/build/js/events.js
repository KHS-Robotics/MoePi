System.register([], function (exports_1, context_1) {
    "use strict";
    var Emitter;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [],
        execute: function () {
            Emitter = class Emitter {
                constructor() {
                    this.handlers = {};
                }
                addEventListener(type, listener, options) {
                    if (!(type in this.handlers))
                        this.handlers[type] = [];
                    this.handlers[type].push(listener);
                }
                removeEventListener(event, callback) {
                    if (!(event in this.handlers))
                        return false;
                    var toRemove;
                    let handlers = this.handlers[event];
                    for (var i in handlers)
                        if (handlers[i] == callback)
                            toRemove.push(i);
                    for (var i of toRemove)
                        delete handlers[i];
                    return toRemove.length > 0;
                }
                dispatchEvent(event) {
                    Object.defineProperty(event, 'target', { value: this });
                    var handlers = this.handlers[event.type];
                    if (!handlers)
                        return event.defaultPrevented;
                    for (var handler of handlers) {
                        if (handler.handleEvent)
                            handler.handleEvent(event);
                        else
                            handler(event);
                        if (event.defaultPrevented)
                            return false;
                    }
                    return true;
                }
            };
            exports_1("Emitter", Emitter);
        }
    };
});
//# sourceMappingURL=events.js.map