System.register([], function (exports_1, context_1) {
    "use strict";
    var RendererState, RenderPipeline;
    var __moduleName = context_1 && context_1.id;
    function rectanglesIntersect(r1, r2) {
        return !(r2.left > r1.left + r1.width
            || r2.left + r2.width < r1.left
            || r2.top > r1.top + r1.height
            || r2.top + r2.height < r1.top);
    }
    function rectanglesOuter(a, b) {
        if (!b)
            return a;
        var top = Math.min(a.top, b.top);
        var left = Math.min(a.left, b.left);
        var right = Math.max(a.left + a.width, b.left + b.width);
        var bottom = Math.max(a.top + a.height, b.top + b.height);
        return { top: top, left: left, width: right - left, height: bottom - top };
    }
    return {
        setters: [],
        execute: function () {
            (function (RendererState) {
                RendererState[RendererState["STOPPED"] = 0] = "STOPPED";
                RendererState[RendererState["RUNNING"] = 1] = "RUNNING";
                RendererState[RendererState["PAUSED"] = 2] = "PAUSED";
                RendererState[RendererState["DEAD"] = 3] = "DEAD";
            })(RendererState || (RendererState = {}));
            exports_1("RendererState", RendererState);
            RenderPipeline = class RenderPipeline {
                constructor() {
                    this.renderers = [];
                    this.eh = [];
                }
                add(renderer, index) {
                    var i = index || this.renderers.length;
                    this.renderers[i] = renderer;
                    var eh = this.eh[i] = this.doHandleEvent.bind(this, i);
                    renderer.addEventListener('renderer.start', eh);
                    renderer.addEventListener('renderer.stop', eh);
                    renderer.addEventListener('renderer.clobber', eh);
                    renderer.addEventListener('renderer.clear', eh);
                }
                clobber(fromIndex, toIndex, rect) {
                    var clobbered = rect;
                    for (var i = fromIndex; i < toIndex; i++) {
                        var renderer = this.renderers[i];
                        if (renderer && rectanglesIntersect(clobbered, renderer.getBounds())) {
                            var r = renderer.refresh();
                            if (r)
                                clobbered = rectanglesOuter(renderer.refresh(), clobbered);
                        }
                    }
                }
                doHandleEvent(idx, e) {
                    var self = this.eh[idx];
                    var target = e.detail.renderer;
                    switch (e.type) {
                        case 'renderer.start':
                            break;
                        case 'renderer.stop':
                            target.removeEventListener('renderer.stop', self);
                            target.removeEventListener('renderer.clobber', self);
                            target.removeEventListener('renderer.clear', self);
                            target.removeEventListener('renderer.pause', self);
                            target.removeEventListener('renderer.resume', self);
                            break;
                        case 'renderer.clobber':
                            this.clobber(idx + 1, this.renderers.length, e.detail.rect);
                            break;
                        case 'renderer.clear':
                            this.clobber(0, idx, e.detail.rect);
                            break;
                    }
                }
            };
            exports_1("RenderPipeline", RenderPipeline);
        }
    };
});
//# sourceMappingURL=renderer.js.map