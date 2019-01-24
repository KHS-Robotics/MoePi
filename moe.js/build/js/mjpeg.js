System.register(["./renderer", "./events"], function (exports_1, context_1) {
    "use strict";
    var renderer_1, events_1, MJPEGVideoStreamDecoder;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [
            function (renderer_1_1) {
                renderer_1 = renderer_1_1;
            },
            function (events_1_1) {
                events_1 = events_1_1;
            }
        ],
        execute: function () {
            MJPEGVideoStreamDecoder = class MJPEGVideoStreamDecoder extends events_1.Emitter {
                constructor(options) {
                    super();
                    this.state = renderer_1.RendererState.STOPPED;
                    this.lastFrame = null;
                    this.ctx = options.ctx;
                    this.bounds = options.bounds || { top: 0, left: 0, width: this.ctx.canvas.width, height: this.ctx.canvas.height };
                }
                _encodeFrame(data) {
                    var binary = '';
                    for (var i = 0; i < data.length; i++)
                        binary += String.fromCharCode(data[i]);
                    var output = window.btoa(binary);
                    var url = "data:image/jpeg;base64," + output;
                    return url;
                }
                transition(origin, dest, name) {
                    if (this.state != origin)
                        return;
                    this.state = dest;
                    this.dispatchEvent(new CustomEvent(name, { detail: { renderer: this } }));
                }
                start() {
                    this.transition(renderer_1.RendererState.STOPPED, renderer_1.RendererState.RUNNING, 'renderer.start');
                }
                pause() {
                    this.transition(renderer_1.RendererState.RUNNING, renderer_1.RendererState.PAUSED, 'renderer.pause');
                }
                resume() {
                    this.transition(renderer_1.RendererState.PAUSED, renderer_1.RendererState.RUNNING, 'renderer.resume');
                }
                stop() {
                    this.state = renderer_1.RendererState.STOPPED;
                    this.lastFrame = null;
                    this.dispatchEvent(new CustomEvent('renderer.stop', { detail: { renderer: this } }));
                }
                getBounds() {
                    return this.bounds;
                }
                setBounds(bounds) {
                    this.bounds = bounds;
                }
                getState() {
                    return this.state;
                }
                offerPacket(packet) {
                    if (this.state != renderer_1.RendererState.RUNNING)
                        return;
                    var image = new Image();
                    image.onload = e => {
                        this.lastFrame = image;
                        const rect = this.bounds;
                        this.ctx.clearRect(rect.top, rect.left, rect.width, rect.height);
                        this.ctx.drawImage(image, rect.top, rect.left, rect.width, rect.height);
                        this.dispatchEvent(new CustomEvent('renderer.clobber', { detail: { renderer: this, rect: rect } }));
                    };
                    image.src = this._encodeFrame(new Uint8Array(packet.getArrayBuffer(), 12));
                }
                refresh() {
                    if (this.lastFrame && this.state == renderer_1.RendererState.RUNNING) {
                        const rect = this.bounds;
                        this.ctx.clearRect(rect.top, rect.left, rect.width, rect.height);
                        this.ctx.drawImage(this.lastFrame, rect.top, rect.left, rect.width, rect.height);
                        return this.bounds;
                    }
                    return null;
                }
            };
            exports_1("MJPEGVideoStreamDecoder", MJPEGVideoStreamDecoder);
        }
    };
});
//# sourceMappingURL=mjpeg.js.map