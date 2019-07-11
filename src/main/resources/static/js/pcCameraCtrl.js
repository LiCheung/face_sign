(function () {
    // pc端浏览器控制摄像头类
    function PcCameraCtrl () {
        var _this = this
        return new Promise(function (resolve, reject) {
            var checkBrowser = _this.checkBrowser()
            if (!checkBrowser) {
                _this.getAllCameras().then(function (camerasArr) {
                    if (camerasArr[0]) {
                        _this.getCameraMediaStream(camerasArr[0].deviceId).then(function (stream) {
                            resolve(stream)
                        })
                    } else {
                        reject(new Error('no cameras'))
                    }
                })
            } else {
                alert('您的浏览器不支持摄像头操作')
                reject(new Error('您的浏览器不支持摄像头操作'))
                console.log(checkBrowser)
            }
        })
    }
    PcCameraCtrl.prototype.checkBrowser = function () {
        var errObj = null
        if (navigator.mediaDevices === undefined ||
            navigator.mediaDevices.enumerateDevices === undefined ||
            navigator.mediaDevices.getUserMedia === undefined) {
            var fctName = '-'
            if (navigator.mediaDevices === undefined) {
                fctName = 'navigator.mediaDevices'
            } else if (navigator.mediaDevices.enumerateDevices === undefined) {
                fctName = 'navigator.mediaDevices.enumerateDevices'
            } else if (navigator.mediaDevices.getUserMedia === undefined) {
                fctName = 'navigator.mediaDevices.getUserMedia'
            }
            errObj = {
                pass: null,
                msg: 'WebRTC issue-! ' + fctName + ' not present in your browser'
            }
        }
        return errObj
    }
    PcCameraCtrl.prototype.getAllCameras = function () {
        return new Promise(function (reslove) {
            navigator.mediaDevices.enumerateDevices().then(function (sourceInfos) {
                var exArray = []
                for (var i = 0; i < sourceInfos.length; ++i) {
                    if (sourceInfos[i].kind == 'videoinput') {
                        // exArray.push(sourceInfos[i].deviceId);
                        // 下标为0是前置摄像头，1为后置摄像头，所以PC不能进入该判断，否则画面会保持在第一帧不动
                        exArray.push(sourceInfos[i])
                    }
                }
                reslove(exArray)
            })
        })
    }
    PcCameraCtrl.prototype.getCameraMediaStream = function (deviceId) {
        return new Promise(function (reslove, reject) {
            if (navigator.getUserMedia) {
                // 该方法可以传递3个参数，分别为获取媒体信息的配置，成功的回调函数和失败的回调函数
                navigator.getUserMedia({
                    audio: false, // 表明是否获取音频
                    video: {  // 对视频信息进行配置
                        optional: [{
                            'sourceId': deviceId
                        }]
                    },
                }, function successFunc(stream) {
                    reslove(stream)
                }, function errorFunc(e) {
                    alert('Error！' + e)
                    reject()
                })
            } else {
                alert('Native device media streaming (getUserMedia) not supported in this browser.')
                reject()
            }
        })
    }

    // 实例化pc端摄像头并创建canvas对象实现拍照功能
    function initPcCameraAndCut (wrapperId, cut) {
        return new Promise(function (resolve) {
            var width = 400
            var height = 300

            var videoId = 'pcCameraVideo'
            var canvasId = 'pcCameraCanvas'

            var wrapperEl = document.querySelector('#' + wrapperId)
            var videoEl = document.createElement('video')
            var canvasEl = document.createElement('canvas')

            videoEl.setAttribute('width', width)
            videoEl.setAttribute('height', height)
            videoEl.setAttribute('id', videoId)
            videoEl.setAttribute('autoplay', 'autoplay')
            videoEl.setAttribute('playsinline', 'playsinline')
            canvasEl.setAttribute('width', width)
            canvasEl.setAttribute('height', height)
            canvasEl.style.display = 'none'
            canvasEl.setAttribute('id', canvasId)

            wrapperEl.appendChild(videoEl)
            wrapperEl.appendChild(canvasEl)

            new PcCameraCtrl().then(function (stream) {
                var video = videoEl
                // 对FireFox进行兼容，这里对返回流数据的处理不同
                if (video.mozSrcObject !== undefined) {
                    //Firefox中，video.mozSrcObject最初为null，而不是未定义的，我们可以靠这个来检测Firefox的支持
                    video.mozSrcObject = stream;
                } else {
                    // 一般的浏览器需要使用createObjectURL对流数据进行处理，再交给video元素的src
                    try {
                        video.src =  window.URL.createObjectURL(stream);
                    }catch (e){
                        video.srcObject = stream;
                    }
                }

                // 画布
                var ctx = canvasEl.getContext('2d')
                var  canvasT = setInterval(function() {
                    ctx.drawImage(videoEl,0,0,width,height)
                }, 10)
                Canvas2Image.saveAsPNG(canvasT);

                resolve({
                    videoEl: videoEl,
                    canvasEl: canvasEl,
                    cutImgFn: function () {
                        return canvasEl.toDataURL('image/png')
                    },
                    destroyCanvasT: function () {
                        clearInterval(canvasT)
                    }
                })

            })
        })
    }

    window.PcCameraCtrl = PcCameraCtrl
    window.initPcCameraAndCut = initPcCameraAndCut
})()