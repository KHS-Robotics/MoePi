System.register(["./websock", "./channels", "./mjpeg", "./h264", "./events", "./overlay", "./renderer"], function (exports_1, context_1) {
    "use strict";
    var websock_1, channels_1, mjpeg_1, h264_1, events_1, overlay_1, renderer_1, wsAddr, wsProtocol, State, BackgroundRenderer, StageManager;
    var __moduleName = context_1 && context_1.id;
    return {
        setters: [
            function (websock_1_1) {
                websock_1 = websock_1_1;
            },
            function (channels_1_1) {
                channels_1 = channels_1_1;
            },
            function (mjpeg_1_1) {
                mjpeg_1 = mjpeg_1_1;
            },
            function (h264_1_1) {
                h264_1 = h264_1_1;
            },
            function (events_1_1) {
                events_1 = events_1_1;
            },
            function (overlay_1_1) {
                overlay_1 = overlay_1_1;
            },
            function (renderer_1_1) {
                renderer_1 = renderer_1_1;
            }
        ],
        execute: function () {
            wsAddr = 'ws://' + window.location.host + '/vdc.ws';
            wsProtocol = 'v0.moews';
            (function (State) {
                State[State["INITIALIZING"] = 0] = "INITIALIZING";
                State[State["WAITING"] = 1] = "WAITING";
                State[State["CONNECTING"] = 2] = "CONNECTING";
                State[State["LOADING"] = 3] = "LOADING";
                State[State["STREAMING"] = 4] = "STREAMING";
                State[State["DISCONNECTED"] = 5] = "DISCONNECTED";
                State[State["ERROR"] = 6] = "ERROR";
            })(State || (State = {}));
            BackgroundRenderer = class BackgroundRenderer extends events_1.Emitter {
                constructor(canvas, context, mgr) {
                    super();
                    this.state = renderer_1.RendererState.STOPPED;
                    this.messages = [];
                    this.redraw = true;
                    this.canvas = canvas;
                    this.context = context;
                    this.mgr = mgr;
                    this.canvas.addEventListener('click', e => this.__handleClick(e));
                }
                transition(origin, dest, name) {
                    if (this.state != origin)
                        return false;
                    this.state = dest;
                    this.dispatchEvent(new CustomEvent(name, { detail: { renderer: this } }));
                    return true;
                }
                start() {
                    if (this.state != renderer_1.RendererState.STOPPED)
                        return;
                    this.state = renderer_1.RendererState.RUNNING;
                    this.render();
                    this.dispatchEvent(new CustomEvent('renderer.start', { detail: { renderer: this } }));
                }
                pause() {
                    this.transition(renderer_1.RendererState.RUNNING, renderer_1.RendererState.PAUSED, 'renderer.pause');
                }
                resume() {
                    this.transition(renderer_1.RendererState.PAUSED, renderer_1.RendererState.RUNNING, 'renderer.resume');
                }
                stop() {
                    this.state = renderer_1.RendererState.STOPPED;
                    this.dispatchEvent(new CustomEvent('renderer.stop', { detail: { renderer: this } }));
                }
                getBounds() {
                    return { top: 0, left: 0, width: this.canvas.width, height: this.canvas.height };
                }
                setBounds(bounds) {
                    return;
                }
                getState() {
                    return this.state;
                }
                refresh() {
                    const ctx = this.context;
                    const cvs = ctx.canvas;
                    const w = cvs.width;
                    const h = cvs.height;
                    ctx.fillStyle = '#4CAF50';
                    ctx.fillRect(0, 0, w, h);
                    ctx.fillStyle = '#000';
                    ctx.font = "100px Arial";
                    ctx.textAlign = 'center';
                    var titleWidth = ctx.measureText('MOE.js').width;
                    var stateTextLeft = (w - titleWidth) / 2 + 5;
                    ctx.fillText('MOE.js', w / 2, h * .3);
                    ctx.font = '36px Arial';
                    ctx.textAlign = 'left';
                    ctx.fillText('[' + State[this.mgr.state].toLowerCase() + ']', stateTextLeft, h * .3 + 30);
                    switch (this.mgr.state) {
                        case State.WAITING:
                            ctx.fillStyle = '#00E676';
                            ctx.fillRect((w - titleWidth) / 2, h / 2, titleWidth, 100);
                            ctx.textAlign = 'center';
                            ctx.fillStyle = '#fff';
                            ctx.font = '48px Courier';
                            ctx.fillText('CONNECT', w / 2, h / 2 + 62);
                            break;
                    }
                    ctx.fillStyle = 'rgba(0,0,0,.7)';
                    ctx.font = '12px Courier';
                    ctx.textAlign = 'left';
                    for (var i = 0; i < this.messages.length; i++)
                        ctx.fillText(this.messages[i], 10, i * 14 + 14);
                    this.redraw = false;
                    return this.getBounds();
                }
                render() {
                    if (this.redraw) {
                        var clobbered = this.refresh();
                        if (clobbered != null)
                            this.dispatchEvent(new CustomEvent('renderer.clobber', { detail: { renderer: this, rect: clobbered } }));
                    }
                    if (this.state == renderer_1.RendererState.RUNNING || this.state == renderer_1.RendererState.PAUSED)
                        window.setTimeout(x => this.render(), 100);
                }
                log(msg) {
                    this.messages.push(msg);
                    if (this.messages.length > this.canvas.height / 24)
                        this.messages.pop();
                    this.redraw = true;
                }
                __handleClick(e) {
                    switch (this.mgr.state) {
                        case State.WAITING:
                            this.mgr.state = State.CONNECTING;
                            break;
                    }
                }
            };
            StageManager = class StageManager {
                constructor(options) {
                    this.canvas = options.canvas;
                    this.ctx2d = this.canvas.getContext('2d');
                    this.pipeline = new renderer_1.RenderPipeline();
                    this.renderer = new BackgroundRenderer(this.canvas, this.ctx2d, this);
                    window.addEventListener('resize', e => {
                        var rect = this.canvas.getClientRects()[0];
                        this.canvas.width = rect.width;
                        this.canvas.height = rect.height;
                        this.renderer.redraw = true;
                    });
                    this.pipeline.add(this.renderer, 0);
                    window.dispatchEvent(new Event('resize'));
                }
                set state(s) {
                    var oldState = this.__state;
                    this.__state = s;
                    switch (s) {
                        case State.CONNECTING:
                            this.doConnect();
                            break;
                    }
                }
                get state() {
                    return this.__state;
                }
                ready() {
                    return Promise.resolve(this);
                }
                log(msg) {
                    this.renderer.log(msg);
                }
                start() {
                    this.state = State.INITIALIZING;
                    this.renderer.start();
                    this.log('[STAGE] Starting default renderer');
                    this.renderer.resume();
                    if (window.location.hash.indexOf('autoconnect=true') >= 0) {
                        this.log('[AUTOCONNECT] Skipping waiting dialog');
                        this.state = State.CONNECTING;
                        return;
                    }
                    this.state = State.WAITING;
                }
                doConnect() {
                    this.log('[WS] Attempting to connect to ' + wsAddr);
                    this.log('[WS] Connecting with protocol ' + wsProtocol);
                    websock_1.WebsocketDataStream.connect({ url: wsAddr, protocol: wsProtocol })
                        .then(stream => {
                        this.log('[WS] Successfully connected to ' + wsAddr);
                        this.stream = stream;
                        this.log('[WS] Querying channels');
                        this.state = State.LOADING;
                        return this.stream.getAvailableChannels();
                    }, error => {
                        this.log('[WS] Connection failed: ' + (error.message || 'could not connect'));
                        this.deinit();
                        throw error;
                    })
                        .then(channels => this.connectToVideoChannel(channels))
                        .then(channel => {
                        this.videoChannel = channel;
                        this.log('[WS] Subscribed to channel #' + channel.getId());
                        this.log('[WS] =>Type: ' + channels_1.DataChannelMediaType[channel.getMediaType()]);
                        this.log('[WS] Looking up metadata...');
                        return channel.getMetadata();
                    })
                        .then(metadata => this.startStreaming(metadata));
                }
                connectToVideoChannel(channels) {
                    this.log('[WS] Found ' + channels.length + ' channels');
                    console.log(channels);
                    var channel = null;
                    for (var channelID in channels)
                        if ((channel = channels[channelID]).getMediaType() == channels_1.DataChannelMediaType.VIDEO)
                            break;
                    if (channel == null) {
                        this.log('[WS] No video channels found');
                        this.stream.close();
                        this.state = State.INITIALIZING;
                        throw null;
                    }
                    this.log('[WS] Subscribing to channel #' + channel.getId());
                    return channel.subscribe();
                }
                startStreaming(channelMetadata) {
                    var videoData = channelMetadata['video'];
                    this.log('[WS] Video format: ' + videoData.format);
                    var rect = {
                        top: 0,
                        left: 0,
                        width: Number.parseInt(videoData.width || '100'),
                        height: Number.parseInt(videoData.height || '100'),
                    };
                    switch (videoData.format) {
                        case 'MJPEG':
                            this.videoRenderer = new mjpeg_1.MJPEGVideoStreamDecoder({ ctx: this.ctx2d, bounds: rect });
                            break;
                        case 'H.264':
                        case 'H264':
                            this.videoRenderer = new h264_1.H264Renderer({ canvas: this.canvas, bounds: rect, webgl: true, worker: true });
                            break;
                        default:
                            this.log('[ERROR] Unsupported video stream format.');
                            this.deinit();
                            throw null;
                    }
                    this.pipeline.add(this.videoRenderer, 1);
                    this.videoChannel.addEventListener('packet', (e) => this.videoRenderer.offerPacket(e.packet));
                    this.state = State.STREAMING;
                    this.videoRenderer.start();
                    if (('overlayChannelId' in channelMetadata) && (this.overlayChannel = this.stream.getChannel(Number.parseInt(channelMetadata['overlayChannelId']))))
                        return this.overlayChannel.subscribe()
                            .then(channel => this.startOverlay(channel));
                    this.startOverlay(null);
                    return;
                }
                startOverlay(overlayChannel) {
                    if (!this.overlayRenderer) {
                        this.overlayRenderer = new overlay_1.OverlayRenderer(this.videoRenderer.getBounds(), this.canvas);
                        this.pipeline.add(this.overlayRenderer, 2);
                    }
                    else {
                        this.overlayRenderer.setBounds(this.videoRenderer.getBounds());
                    }
                    this.overlayRenderer.setBounds(this.videoRenderer.getBounds());
                    if (this.overlayRenderer.getState() == renderer_1.RendererState.STOPPED)
                        this.overlayRenderer.start();
                    if (overlayChannel)
                        overlayChannel.addEventListener('packet', (e) => this.overlayRenderer.offerPacket(e.packet));
                    return overlayChannel;
                }
                deinit() {
                    var streamFinishPromises = [Promise.resolve()];
                    if (this.videoChannel) {
                        if (this.videoChannel.isSubscribed())
                            streamFinishPromises.push(this.videoChannel.unsubscribe());
                        this.videoChannel = null;
                    }
                    if (this.videoRenderer && this.videoRenderer.getState() == renderer_1.RendererState.RUNNING)
                        this.videoRenderer.stop();
                    if (this.overlayChannel) {
                        if (this.overlayChannel.isSubscribed())
                            streamFinishPromises.push(this.overlayChannel.unsubscribe());
                        this.overlayChannel = null;
                    }
                    if (this.overlayRenderer && this.overlayRenderer.getState() == renderer_1.RendererState.RUNNING)
                        this.overlayRenderer.stop();
                    if (this.stream && this.stream.isConnected())
                        Promise.all(streamFinishPromises).then(x => this.stream.close());
                    this.state = State.DISCONNECTED;
                }
            };
            exports_1("StageManager", StageManager);
        }
    };
});
//# sourceMappingURL=init.js.map