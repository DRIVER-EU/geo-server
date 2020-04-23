webpackJsonp([1],{"7Otq":function(e,t,a){e.exports=a.p+"static/img/logo.a9886be.png"},"7zck":function(e,t){},NHnr:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var r=a("/5sW"),s=a("mtWM"),i=a.n(s),n={name:"MessageForm",data:function(){return{snackbarSuccess:!1,snackbarError:!1,snackbarSuccessText:"",snackbarErrorText:"",valid:!0,url:"",title:"",dataType:"",items:[{text:"bmp",value:"image_bmp"},{text:"gif",value:"image_gif"},{text:"geotiff",value:"image_geotiff"},{text:"jpeg",value:"image_jpeg"},{text:"png",value:"image_png"},{text:"audio_mpeg",value:"audio_mpeg"},{text:"video_mpeg",value:"video_mpeg"},{text:"msvideo",value:"video_msvideo"},{text:"avi",value:"video_avi"},{text:"msword",value:"msword"},{text:"pdf",value:"pdf"},{text:"excel",value:"excel"},{text:"powerpoint",value:"powerpoint"},{text:"zip",value:"zip"},{text:"other",value:"other"},{text:"json",value:"json"},{text:"geoJson",value:"geojson"},{text:"text_plain",value:"text_plain"},{text:"vorbis",value:"audio_vorbis"},{text:"ogg",value:"ogg"}],description:""}},methods:{submit:function(){var e=this;this.$refs.form.validate()&&i.a.post("http://localhost:8190/CISRestAdaptor/sendLargeDataUpdateJson",{url:this.url,dataType:this.dataType,title:this.title,description:this.description}).then(function(t){e.snackbarSuccess=!0,e.snackbarSuccessText="Data was successfully submitted.",e.clear()}).catch(function(t){e.snackbarError=!0,e.snackbarErrorText="Data was not successfully submitted."})},clear:function(){this.$refs.form.reset(),this.valid=!0}}},c={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticStyle:{margin:"auto"}},[a("h4",{staticClass:"primary--text",staticStyle:{"padding-bottom":"20px"}},[e._v("\n    Use this form to inform others that there is new content available and where they can find it.\n  ")]),e._v(" "),a("v-card",{staticClass:"messageForm"},[a("v-form",{ref:"form",attrs:{"lazy-validation":""},model:{value:e.valid,callback:function(t){e.valid=t},expression:"valid"}},[a("v-flex",[a("v-text-field",{attrs:{label:"URL",required:"",rules:[function(e){return!!e||"URL is required"}],placeholder:"Enter a valid URL (required)"},model:{value:e.url,callback:function(t){e.url=t},expression:"url"}})],1),e._v(" "),a("v-select",{attrs:{label:"Select data type",items:e.items,required:"",rules:[function(e){return!!e||"Data type is required"}],autocomplete:""},model:{value:e.dataType,callback:function(t){e.dataType=t},expression:"dataType"}}),e._v(" "),a("v-text-field",{attrs:{label:"Title",required:"",rules:[function(e){return!!e||"Title is required"}],placeholder:"Enter a title (required)"},model:{value:e.title,callback:function(t){e.title=t},expression:"title"}}),e._v(" "),a("v-text-field",{attrs:{label:"Description",placeholder:"Enter a description (optional)"},model:{value:e.description,callback:function(t){e.description=t},expression:"description"}})],1),e._v(" "),a("v-card-actions",[a("v-spacer"),e._v(" "),a("v-btn",{attrs:{disabled:!e.valid},on:{click:e.submit}},[e._v("\n        submit\n      ")]),e._v(" "),a("v-btn",{on:{click:e.clear}},[e._v("clear")])],1),e._v(" "),a("v-snackbar",{staticClass:"snackbarSuccess",attrs:{top:!0},model:{value:e.snackbarSuccess,callback:function(t){e.snackbarSuccess=t},expression:"snackbarSuccess"}},[e._v("\n      "+e._s(e.snackbarSuccessText)+"\n      "),a("v-btn",{attrs:{flat:"",color:"white"},nativeOn:{click:function(t){e.snackbarSuccess=!1}}},[e._v("Close")])],1),e._v(" "),a("v-snackbar",{staticClass:"snackbarError",attrs:{top:!0},model:{value:e.snackbarError,callback:function(t){e.snackbarError=t},expression:"snackbarError"}},[e._v("\n      "+e._s(e.snackbarErrorText)+"\n      "),a("v-btn",{attrs:{flat:"",color:"white"},nativeOn:{click:function(t){e.snackbarError=!1}}},[e._v("Close")])],1)],1)],1)},staticRenderFns:[]};var l={components:{MessageForm:a("VU/8")(n,c,!1,function(e){a("x8O9")},"data-v-f34f896c",null).exports},name:"App"},o={render:function(){var e=this.$createElement,t=this._self._c||e;return t("v-app",[t("v-toolbar",{staticClass:"primary"},[t("img",{staticClass:"project-logo",attrs:{src:a("7Otq")}}),this._v(" "),t("v-toolbar-title",{staticClass:"title"},[this._v("Data Update Tool")]),this._v(" "),t("v-spacer")],1),this._v(" "),t("main",[t("message-form")],1)],1)},staticRenderFns:[]};var u=a("VU/8")(l,o,!1,function(e){a("qEph")},null,null).exports,v=a("3EgV"),d=a.n(v);a("7zck");r.a.use(d.a,{theme:{primary:"#FDB836",secondary:"#b0bec5",tertiary:"#fff8dc7a",accent:"#8c9eff",error:"#b71c1c"}}),r.a.config.productionTip=!0,new r.a({el:"#app",render:function(e){return e(u)}})},qEph:function(e,t){},x8O9:function(e,t){}},["NHnr"]);
//# sourceMappingURL=app.e4778f908d8c5d8c9376.js.map