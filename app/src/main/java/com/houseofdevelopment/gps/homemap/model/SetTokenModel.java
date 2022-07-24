package com.houseofdevelopment.gps.homemap.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SetTokenModel {

    @SerializedName("host")
    @Expose
    private String host;
    @SerializedName("eid")
    @Expose
    private String eid;
    @SerializedName("gis_sid")
    @Expose
    private String gisSid;
    @SerializedName("au")
    @Expose
    private String au;
    @SerializedName("tm")
    @Expose
    private Integer tm;
    @SerializedName("wsdk_version")
    @Expose
    private String wsdkVersion;
    @SerializedName("api")
    @Expose
    private String api;
    @SerializedName("local_version")
    @Expose
    private String localVersion;
    @SerializedName("env")
    @Expose
    private Env env;
    @SerializedName("base_url")
    @Expose
    private String baseUrl;
    @SerializedName("hw_gw_ip")
    @Expose
    private String hwGwIp;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("th")
    @Expose
    private String th;
    @SerializedName("classes")
    @Expose
    private Classes classes;
    @SerializedName("features")
    @Expose
    private Features features;
    @SerializedName("error")
    @Expose
    private String error;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getGisSid() {
        return gisSid;
    }

    public void setGisSid(String gisSid) {
        this.gisSid = gisSid;
    }

    public String getAu() {
        return au;
    }

    public void setAu(String au) {
        this.au = au;
    }

    public Integer getTm() {
        return tm;
    }

    public void setTm(Integer tm) {
        this.tm = tm;
    }

    public String getWsdkVersion() {
        return wsdkVersion;
    }

    public void setWsdkVersion(String wsdkVersion) {
        this.wsdkVersion = wsdkVersion;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getLocalVersion() {
        return localVersion;
    }

    public void setLocalVersion(String localVersion) {
        this.localVersion = localVersion;
    }

    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHwGwIp() {
        return hwGwIp;
    }

    public void setHwGwIp(String hwGwIp) {
        this.hwGwIp = hwGwIp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTh() {
        return th;
    }

    public void setTh(String th) {
        this.th = th;
    }

    public Classes getClasses() {
        return classes;
    }

    public void setClasses(Classes classes) {
        this.classes = classes;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public class User {

        @SerializedName("nm")
        @Expose
        public String nm;
        @SerializedName("cls")
        @Expose
        private Integer cls;
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("prp")
        @Expose
        private Prp prp;
        @SerializedName("crt")
        @Expose
        private Integer crt;
        @SerializedName("bact")
        @Expose
        public Integer bact;
        @SerializedName("mu")
        @Expose
        private Integer mu;
        @SerializedName("ct")
        @Expose
        private Integer ct;
        @SerializedName("ftp")
        @Expose
        private Ftp ftp;
        @SerializedName("fl")
        @Expose
        private Integer fl;
        @SerializedName("hm")
        @Expose
        private String hm;
        @SerializedName("ld")
        @Expose
        private Integer ld;
        @SerializedName("pfl")
        @Expose
        private Integer pfl;
        @SerializedName("ap")
        @Expose
        private Object ap;
        @SerializedName("mapps")
        @Expose
        private Mapps mapps;
        @SerializedName("mappsmax")
        @Expose
        private Integer mappsmax;
        @SerializedName("uacl")
        @Expose
        private Integer uacl;

        public String getNm() {
            return nm;
        }

        public void setNm(String nm) {
            this.nm = nm;
        }

        public Integer getCls() {
            return cls;
        }

        public void setCls(Integer cls) {
            this.cls = cls;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Prp getPrp() {
            return prp;
        }

        public void setPrp(Prp prp) {
            this.prp = prp;
        }

        public Integer getCrt() {
            return crt;
        }

        public void setCrt(Integer crt) {
            this.crt = crt;
        }

        public Integer getBact() {
            return bact;
        }

        public void setBact(Integer bact) {
            this.bact = bact;
        }

        public Integer getMu() {
            return mu;
        }

        public void setMu(Integer mu) {
            this.mu = mu;
        }

        public Integer getCt() {
            return ct;
        }

        public void setCt(Integer ct) {
            this.ct = ct;
        }

        public Ftp getFtp() {
            return ftp;
        }

        public void setFtp(Ftp ftp) {
            this.ftp = ftp;
        }

        public Integer getFl() {
            return fl;
        }

        public void setFl(Integer fl) {
            this.fl = fl;
        }

        public String getHm() {
            return hm;
        }

        public void setHm(String hm) {
            this.hm = hm;
        }

        public Integer getLd() {
            return ld;
        }

        public void setLd(Integer ld) {
            this.ld = ld;
        }

        public Integer getPfl() {
            return pfl;
        }

        public void setPfl(Integer pfl) {
            this.pfl = pfl;
        }

        public Object getAp() {
            return ap;
        }

        public void setAp(Object ap) {
            this.ap = ap;
        }

        public Mapps getMapps() {
            return mapps;
        }

        public void setMapps(Mapps mapps) {
            this.mapps = mapps;
        }

        public Integer getMappsmax() {
            return mappsmax;
        }

        public void setMappsmax(Integer mappsmax) {
            this.mappsmax = mappsmax;
        }

        public Integer getUacl() {
            return uacl;
        }

        public void setUacl(Integer uacl) {
            this.uacl = uacl;
        }

        public class Mapps {
        }
        public class Prp {

            @SerializedName("access_templates")
            @Expose
            private String accessTemplates;
            @SerializedName("dst")
            @Expose
            private String dst;
            @SerializedName("fpnl")
            @Expose
            private String fpnl;
            @SerializedName("geodata_source")
            @Expose
            private String geodataSource;
            @SerializedName("hbacit")
            @Expose
            private String hbacit;
            @SerializedName("hpnl")
            @Expose
            private String hpnl;
            @SerializedName("language")
            @Expose
            private String language;
            @SerializedName("minimap_zoom_level")
            @Expose
            private String minimapZoomLevel;
            @SerializedName("mont")
            @Expose
            private String mont;
            @SerializedName("monu")
            @Expose
            private String monu;
            @SerializedName("monuv")
            @Expose
            private String monuv;
            @SerializedName("mtg")
            @Expose
            private String mtg;
            @SerializedName("mtve")
            @Expose
            private String mtve;
            @SerializedName("mu_fast_report_tmpl")
            @Expose
            private String muFastReportTmpl;
            @SerializedName("mu_fast_track_ival")
            @Expose
            private String muFastTrackIval;
            @SerializedName("mu_tbl_cols_sizes")
            @Expose
            private String muTblColsSizes;
            @SerializedName("mu_tbl_sort")
            @Expose
            private String muTblSort;
            @SerializedName("show_log")
            @Expose
            private String showLog;
            @SerializedName("tz")
            @Expose
            private String tz;
            @SerializedName("umap")
            @Expose
            private String umap;
            @SerializedName("us_addr_fmt")
            @Expose
            private String usAddrFmt;
            @SerializedName("us_addr_ordr")
            @Expose
            private String usAddrOrdr;
            @SerializedName("znsvlist")
            @Expose
            private String znsvlist;

            public String getAccessTemplates() {
                return accessTemplates;
            }

            public void setAccessTemplates(String accessTemplates) {
                this.accessTemplates = accessTemplates;
            }

            public String getDst() {
                return dst;
            }

            public void setDst(String dst) {
                this.dst = dst;
            }

            public String getFpnl() {
                return fpnl;
            }

            public void setFpnl(String fpnl) {
                this.fpnl = fpnl;
            }

            public String getGeodataSource() {
                return geodataSource;
            }

            public void setGeodataSource(String geodataSource) {
                this.geodataSource = geodataSource;
            }

            public String getHbacit() {
                return hbacit;
            }

            public void setHbacit(String hbacit) {
                this.hbacit = hbacit;
            }

            public String getHpnl() {
                return hpnl;
            }

            public void setHpnl(String hpnl) {
                this.hpnl = hpnl;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public String getMinimapZoomLevel() {
                return minimapZoomLevel;
            }

            public void setMinimapZoomLevel(String minimapZoomLevel) {
                this.minimapZoomLevel = minimapZoomLevel;
            }

            public String getMont() {
                return mont;
            }

            public void setMont(String mont) {
                this.mont = mont;
            }

            public String getMonu() {
                return monu;
            }

            public void setMonu(String monu) {
                this.monu = monu;
            }

            public String getMonuv() {
                return monuv;
            }

            public void setMonuv(String monuv) {
                this.monuv = monuv;
            }

            public String getMtg() {
                return mtg;
            }

            public void setMtg(String mtg) {
                this.mtg = mtg;
            }

            public String getMtve() {
                return mtve;
            }

            public void setMtve(String mtve) {
                this.mtve = mtve;
            }

            public String getMuFastReportTmpl() {
                return muFastReportTmpl;
            }

            public void setMuFastReportTmpl(String muFastReportTmpl) {
                this.muFastReportTmpl = muFastReportTmpl;
            }

            public String getMuFastTrackIval() {
                return muFastTrackIval;
            }

            public void setMuFastTrackIval(String muFastTrackIval) {
                this.muFastTrackIval = muFastTrackIval;
            }

            public String getMuTblColsSizes() {
                return muTblColsSizes;
            }

            public void setMuTblColsSizes(String muTblColsSizes) {
                this.muTblColsSizes = muTblColsSizes;
            }

            public String getMuTblSort() {
                return muTblSort;
            }

            public void setMuTblSort(String muTblSort) {
                this.muTblSort = muTblSort;
            }

            public String getShowLog() {
                return showLog;
            }

            public void setShowLog(String showLog) {
                this.showLog = showLog;
            }

            public String getTz() {
                return tz;
            }

            public void setTz(String tz) {
                this.tz = tz;
            }

            public String getUmap() {
                return umap;
            }

            public void setUmap(String umap) {
                this.umap = umap;
            }

            public String getUsAddrFmt() {
                return usAddrFmt;
            }

            public void setUsAddrFmt(String usAddrFmt) {
                this.usAddrFmt = usAddrFmt;
            }

            public String getUsAddrOrdr() {
                return usAddrOrdr;
            }

            public void setUsAddrOrdr(String usAddrOrdr) {
                this.usAddrOrdr = usAddrOrdr;
            }

            public String getZnsvlist() {
                return znsvlist;
            }

            public void setZnsvlist(String znsvlist) {
                this.znsvlist = znsvlist;
            }

        }
    }
    public static class Features {

        @SerializedName("unlim")
        @Expose
        private Integer unlim;
        @SerializedName("svcs")
        @Expose
        private Svcs svcs;

        public Integer getUnlim() {
            return unlim;
        }

        public void setUnlim(Integer unlim) {
            this.unlim = unlim;
        }

        public Svcs getSvcs() {
            return svcs;
        }

        public void setSvcs(Svcs svcs) {
            this.svcs = svcs;
        }

        public class Svcs {

            @SerializedName("admin_fields")
            @Expose
            private Integer adminFields;
            @SerializedName("app.1432623812_29463")
            @Expose
            private Integer app143262381229463;
            @SerializedName("app.1432624629_30061")
            @Expose
            private Integer app143262462930061;
            @SerializedName("app.1432624649_30076")
            @Expose
            private Integer app143262464930076;
            @SerializedName("app.1432624671_30089")
            @Expose
            private Integer app143262467130089;
            @SerializedName("app.1432624682_30101")
            @Expose
            private Integer app143262468230101;
            @SerializedName("app.1432624701_30116")
            @Expose
            private Integer app143262470130116;
            @SerializedName("app.1432624763_30156")
            @Expose
            private Integer app143262476330156;
            @SerializedName("app.1432624778_30167")
            @Expose
            private Integer app143262477830167;
            @SerializedName("app.1432624792_30180")
            @Expose
            private Integer app143262479230180;
            @SerializedName("app.1432624804_30187")
            @Expose
            private Integer app143262480430187;
            @SerializedName("app.1432624817_30195")
            @Expose
            private Integer app143262481730195;
            @SerializedName("app.1432624827_30204")
            @Expose
            private Integer app143262482730204;
            @SerializedName("app.1432624839_30217")
            @Expose
            private Integer app143262483930217;
            @SerializedName("app.1432624854_30227")
            @Expose
            private Integer app143262485430227;
            @SerializedName("app.1531731260_2778")
            @Expose
            private Integer app15317312602778;
            @SerializedName("app.1531731261_3011")
            @Expose
            private Integer app15317312613011;
            @SerializedName("app.1531731262_3145")
            @Expose
            private Integer app15317312623145;
            @SerializedName("app.1599482691_2704597")
            @Expose
            private Integer app15994826912704597;
            @SerializedName("auto.cms_manager.0")
            @Expose
            private Integer autoCmsManager0;
            @SerializedName("auto.wialon_activex")
            @Expose
            private Integer autoWialonActivex;
            @SerializedName("auto.wialon_web.0")
            @Expose
            private Integer autoWialonWeb0;
            @SerializedName("auto.wialon_web.1")
            @Expose
            private Integer autoWialonWeb1;
            @SerializedName("auto.wialon_web.2")
            @Expose
            private Integer autoWialonWeb2;
            @SerializedName("avl_resource")
            @Expose
            private Integer avlResource;
            @SerializedName("avl_retranslator")
            @Expose
            private Integer avlRetranslator;
            @SerializedName("avl_route")
            @Expose
            private Integer avlRoute;
            @SerializedName("avl_unit")
            @Expose
            private Integer avlUnit;
            @SerializedName("avl_unit_group")
            @Expose
            private Integer avlUnitGroup;
            @SerializedName("cms_manager")
            @Expose
            private Integer cmsManager;
            @SerializedName("create_resources")
            @Expose
            private Integer createResources;
            @SerializedName("create_unit_groups")
            @Expose
            private Integer createUnitGroups;
            @SerializedName("create_units")
            @Expose
            private Integer createUnits;
            @SerializedName("create_users")
            @Expose
            private Integer createUsers;
            @SerializedName("custom_fields")
            @Expose
            private Integer customFields;
            @SerializedName("custom_reports")
            @Expose
            private Integer customReports;
            @SerializedName("driver_groups")
            @Expose
            private Integer driverGroups;
            @SerializedName("drivers")
            @Expose
            private Integer drivers;
            @SerializedName("ecodriving")
            @Expose
            private Integer ecodriving;
            @SerializedName("email_notification")
            @Expose
            private Integer emailNotification;
            @SerializedName("email_report")
            @Expose
            private Integer emailReport;
            @SerializedName("google_service")
            @Expose
            private Integer googleService;
            @SerializedName("import_export")
            @Expose
            private Integer importExport;
            @SerializedName("jobs")
            @Expose
            private Integer jobs;
            @SerializedName("locator")
            @Expose
            private Integer locator;
            @SerializedName("messages")
            @Expose
            private Integer messages;
            @SerializedName("mobile_apps")
            @Expose
            private Integer mobileApps;
            @SerializedName("nimbus")
            @Expose
            private Integer nimbus;
            @SerializedName("notifications")
            @Expose
            private Integer notifications;
            @SerializedName("order_routes")
            @Expose
            private Integer orderRoutes;
            @SerializedName("orders")
            @Expose
            private Integer orders;
            @SerializedName("own_google_service")
            @Expose
            private Integer ownGoogleService;
            @SerializedName("own_yandex_service")
            @Expose
            private Integer ownYandexService;
            @SerializedName("pois")
            @Expose
            private Integer pois;
            @SerializedName("profile_fields")
            @Expose
            private Integer profileFields;
            @SerializedName("reportsmngt")
            @Expose
            private Integer reportsmngt;
            @SerializedName("reporttemplates")
            @Expose
            private Integer reporttemplates;
            @SerializedName("retranslator_units")
            @Expose
            private Integer retranslatorUnits;
            @SerializedName("rounds")
            @Expose
            private Integer rounds;
            @SerializedName("route_schedules")
            @Expose
            private Integer routeSchedules;
            @SerializedName("sdk")
            @Expose
            private Integer sdk;
            @SerializedName("seasonal_units")
            @Expose
            private Integer seasonalUnits;
            @SerializedName("service_intervals")
            @Expose
            private Integer serviceIntervals;
            @SerializedName("sms")
            @Expose
            private Integer sms;
            @SerializedName("storage_user")
            @Expose
            private Integer storageUser;
            @SerializedName("tacho")
            @Expose
            private Integer tacho;
            @SerializedName("tag_groups")
            @Expose
            private Integer tagGroups;
            @SerializedName("tags")
            @Expose
            private Integer tags;
            @SerializedName("toll_roads")
            @Expose
            private Integer tollRoads;
            @SerializedName("trailer_groups")
            @Expose
            private Integer trailerGroups;
            @SerializedName("trailers")
            @Expose
            private Integer trailers;
            @SerializedName("unit_commands")
            @Expose
            private Integer unitCommands;
            @SerializedName("unit_sensors")
            @Expose
            private Integer unitSensors;
            @SerializedName("user_notifications")
            @Expose
            private Integer userNotifications;
            @SerializedName("video")
            @Expose
            private Integer video;
            @SerializedName("web_resources")
            @Expose
            private Integer webResources;
            @SerializedName("wialon_activex")
            @Expose
            private Integer wialonActivex;
            @SerializedName("wialon_mobile2")
            @Expose
            private Integer wialonMobile2;
            @SerializedName("wialon_mobile_client")
            @Expose
            private Integer wialonMobileClient;
            @SerializedName("wialon_sdk")
            @Expose
            private Integer wialonSdk;
            @SerializedName("yandex_geocoding")
            @Expose
            private Integer yandexGeocoding;
            @SerializedName("yandex_panorama")
            @Expose
            private Integer yandexPanorama;
            @SerializedName("yandex_routing")
            @Expose
            private Integer yandexRouting;
            @SerializedName("yandex_service")
            @Expose
            private Integer yandexService;
            @SerializedName("zone_groups")
            @Expose
            private Integer zoneGroups;
            @SerializedName("zones_library")
            @Expose
            private Integer zonesLibrary;

            public Integer getAdminFields() {
                return adminFields;
            }

            public void setAdminFields(Integer adminFields) {
                this.adminFields = adminFields;
            }

            public Integer getApp143262381229463() {
                return app143262381229463;
            }

            public void setApp143262381229463(Integer app143262381229463) {
                this.app143262381229463 = app143262381229463;
            }

            public Integer getApp143262462930061() {
                return app143262462930061;
            }

            public void setApp143262462930061(Integer app143262462930061) {
                this.app143262462930061 = app143262462930061;
            }

            public Integer getApp143262464930076() {
                return app143262464930076;
            }

            public void setApp143262464930076(Integer app143262464930076) {
                this.app143262464930076 = app143262464930076;
            }

            public Integer getApp143262467130089() {
                return app143262467130089;
            }

            public void setApp143262467130089(Integer app143262467130089) {
                this.app143262467130089 = app143262467130089;
            }

            public Integer getApp143262468230101() {
                return app143262468230101;
            }

            public void setApp143262468230101(Integer app143262468230101) {
                this.app143262468230101 = app143262468230101;
            }

            public Integer getApp143262470130116() {
                return app143262470130116;
            }

            public void setApp143262470130116(Integer app143262470130116) {
                this.app143262470130116 = app143262470130116;
            }

            public Integer getApp143262476330156() {
                return app143262476330156;
            }

            public void setApp143262476330156(Integer app143262476330156) {
                this.app143262476330156 = app143262476330156;
            }

            public Integer getApp143262477830167() {
                return app143262477830167;
            }

            public void setApp143262477830167(Integer app143262477830167) {
                this.app143262477830167 = app143262477830167;
            }

            public Integer getApp143262479230180() {
                return app143262479230180;
            }

            public void setApp143262479230180(Integer app143262479230180) {
                this.app143262479230180 = app143262479230180;
            }

            public Integer getApp143262480430187() {
                return app143262480430187;
            }

            public void setApp143262480430187(Integer app143262480430187) {
                this.app143262480430187 = app143262480430187;
            }

            public Integer getApp143262481730195() {
                return app143262481730195;
            }

            public void setApp143262481730195(Integer app143262481730195) {
                this.app143262481730195 = app143262481730195;
            }

            public Integer getApp143262482730204() {
                return app143262482730204;
            }

            public void setApp143262482730204(Integer app143262482730204) {
                this.app143262482730204 = app143262482730204;
            }

            public Integer getApp143262483930217() {
                return app143262483930217;
            }

            public void setApp143262483930217(Integer app143262483930217) {
                this.app143262483930217 = app143262483930217;
            }

            public Integer getApp143262485430227() {
                return app143262485430227;
            }

            public void setApp143262485430227(Integer app143262485430227) {
                this.app143262485430227 = app143262485430227;
            }

            public Integer getApp15317312602778() {
                return app15317312602778;
            }

            public void setApp15317312602778(Integer app15317312602778) {
                this.app15317312602778 = app15317312602778;
            }

            public Integer getApp15317312613011() {
                return app15317312613011;
            }

            public void setApp15317312613011(Integer app15317312613011) {
                this.app15317312613011 = app15317312613011;
            }

            public Integer getApp15317312623145() {
                return app15317312623145;
            }

            public void setApp15317312623145(Integer app15317312623145) {
                this.app15317312623145 = app15317312623145;
            }

            public Integer getApp15994826912704597() {
                return app15994826912704597;
            }

            public void setApp15994826912704597(Integer app15994826912704597) {
                this.app15994826912704597 = app15994826912704597;
            }

            public Integer getAutoCmsManager0() {
                return autoCmsManager0;
            }

            public void setAutoCmsManager0(Integer autoCmsManager0) {
                this.autoCmsManager0 = autoCmsManager0;
            }

            public Integer getAutoWialonActivex() {
                return autoWialonActivex;
            }

            public void setAutoWialonActivex(Integer autoWialonActivex) {
                this.autoWialonActivex = autoWialonActivex;
            }

            public Integer getAutoWialonWeb0() {
                return autoWialonWeb0;
            }

            public void setAutoWialonWeb0(Integer autoWialonWeb0) {
                this.autoWialonWeb0 = autoWialonWeb0;
            }

            public Integer getAutoWialonWeb1() {
                return autoWialonWeb1;
            }

            public void setAutoWialonWeb1(Integer autoWialonWeb1) {
                this.autoWialonWeb1 = autoWialonWeb1;
            }

            public Integer getAutoWialonWeb2() {
                return autoWialonWeb2;
            }

            public void setAutoWialonWeb2(Integer autoWialonWeb2) {
                this.autoWialonWeb2 = autoWialonWeb2;
            }

            public Integer getAvlResource() {
                return avlResource;
            }

            public void setAvlResource(Integer avlResource) {
                this.avlResource = avlResource;
            }

            public Integer getAvlRetranslator() {
                return avlRetranslator;
            }

            public void setAvlRetranslator(Integer avlRetranslator) {
                this.avlRetranslator = avlRetranslator;
            }

            public Integer getAvlRoute() {
                return avlRoute;
            }

            public void setAvlRoute(Integer avlRoute) {
                this.avlRoute = avlRoute;
            }

            public Integer getAvlUnit() {
                return avlUnit;
            }

            public void setAvlUnit(Integer avlUnit) {
                this.avlUnit = avlUnit;
            }

            public Integer getAvlUnitGroup() {
                return avlUnitGroup;
            }

            public void setAvlUnitGroup(Integer avlUnitGroup) {
                this.avlUnitGroup = avlUnitGroup;
            }

            public Integer getCmsManager() {
                return cmsManager;
            }

            public void setCmsManager(Integer cmsManager) {
                this.cmsManager = cmsManager;
            }

            public Integer getCreateResources() {
                return createResources;
            }

            public void setCreateResources(Integer createResources) {
                this.createResources = createResources;
            }

            public Integer getCreateUnitGroups() {
                return createUnitGroups;
            }

            public void setCreateUnitGroups(Integer createUnitGroups) {
                this.createUnitGroups = createUnitGroups;
            }

            public Integer getCreateUnits() {
                return createUnits;
            }

            public void setCreateUnits(Integer createUnits) {
                this.createUnits = createUnits;
            }

            public Integer getCreateUsers() {
                return createUsers;
            }

            public void setCreateUsers(Integer createUsers) {
                this.createUsers = createUsers;
            }

            public Integer getCustomFields() {
                return customFields;
            }

            public void setCustomFields(Integer customFields) {
                this.customFields = customFields;
            }

            public Integer getCustomReports() {
                return customReports;
            }

            public void setCustomReports(Integer customReports) {
                this.customReports = customReports;
            }

            public Integer getDriverGroups() {
                return driverGroups;
            }

            public void setDriverGroups(Integer driverGroups) {
                this.driverGroups = driverGroups;
            }

            public Integer getDrivers() {
                return drivers;
            }

            public void setDrivers(Integer drivers) {
                this.drivers = drivers;
            }

            public Integer getEcodriving() {
                return ecodriving;
            }

            public void setEcodriving(Integer ecodriving) {
                this.ecodriving = ecodriving;
            }

            public Integer getEmailNotification() {
                return emailNotification;
            }

            public void setEmailNotification(Integer emailNotification) {
                this.emailNotification = emailNotification;
            }

            public Integer getEmailReport() {
                return emailReport;
            }

            public void setEmailReport(Integer emailReport) {
                this.emailReport = emailReport;
            }

            public Integer getGoogleService() {
                return googleService;
            }

            public void setGoogleService(Integer googleService) {
                this.googleService = googleService;
            }

            public Integer getImportExport() {
                return importExport;
            }

            public void setImportExport(Integer importExport) {
                this.importExport = importExport;
            }

            public Integer getJobs() {
                return jobs;
            }

            public void setJobs(Integer jobs) {
                this.jobs = jobs;
            }

            public Integer getLocator() {
                return locator;
            }

            public void setLocator(Integer locator) {
                this.locator = locator;
            }

            public Integer getMessages() {
                return messages;
            }

            public void setMessages(Integer messages) {
                this.messages = messages;
            }

            public Integer getMobileApps() {
                return mobileApps;
            }

            public void setMobileApps(Integer mobileApps) {
                this.mobileApps = mobileApps;
            }

            public Integer getNimbus() {
                return nimbus;
            }

            public void setNimbus(Integer nimbus) {
                this.nimbus = nimbus;
            }

            public Integer getNotifications() {
                return notifications;
            }

            public void setNotifications(Integer notifications) {
                this.notifications = notifications;
            }

            public Integer getOrderRoutes() {
                return orderRoutes;
            }

            public void setOrderRoutes(Integer orderRoutes) {
                this.orderRoutes = orderRoutes;
            }

            public Integer getOrders() {
                return orders;
            }

            public void setOrders(Integer orders) {
                this.orders = orders;
            }

            public Integer getOwnGoogleService() {
                return ownGoogleService;
            }

            public void setOwnGoogleService(Integer ownGoogleService) {
                this.ownGoogleService = ownGoogleService;
            }

            public Integer getOwnYandexService() {
                return ownYandexService;
            }

            public void setOwnYandexService(Integer ownYandexService) {
                this.ownYandexService = ownYandexService;
            }

            public Integer getPois() {
                return pois;
            }

            public void setPois(Integer pois) {
                this.pois = pois;
            }

            public Integer getProfileFields() {
                return profileFields;
            }

            public void setProfileFields(Integer profileFields) {
                this.profileFields = profileFields;
            }

            public Integer getReportsmngt() {
                return reportsmngt;
            }

            public void setReportsmngt(Integer reportsmngt) {
                this.reportsmngt = reportsmngt;
            }

            public Integer getReporttemplates() {
                return reporttemplates;
            }

            public void setReporttemplates(Integer reporttemplates) {
                this.reporttemplates = reporttemplates;
            }

            public Integer getRetranslatorUnits() {
                return retranslatorUnits;
            }

            public void setRetranslatorUnits(Integer retranslatorUnits) {
                this.retranslatorUnits = retranslatorUnits;
            }

            public Integer getRounds() {
                return rounds;
            }

            public void setRounds(Integer rounds) {
                this.rounds = rounds;
            }

            public Integer getRouteSchedules() {
                return routeSchedules;
            }

            public void setRouteSchedules(Integer routeSchedules) {
                this.routeSchedules = routeSchedules;
            }

            public Integer getSdk() {
                return sdk;
            }

            public void setSdk(Integer sdk) {
                this.sdk = sdk;
            }

            public Integer getSeasonalUnits() {
                return seasonalUnits;
            }

            public void setSeasonalUnits(Integer seasonalUnits) {
                this.seasonalUnits = seasonalUnits;
            }

            public Integer getServiceIntervals() {
                return serviceIntervals;
            }

            public void setServiceIntervals(Integer serviceIntervals) {
                this.serviceIntervals = serviceIntervals;
            }

            public Integer getSms() {
                return sms;
            }

            public void setSms(Integer sms) {
                this.sms = sms;
            }

            public Integer getStorageUser() {
                return storageUser;
            }

            public void setStorageUser(Integer storageUser) {
                this.storageUser = storageUser;
            }

            public Integer getTacho() {
                return tacho;
            }

            public void setTacho(Integer tacho) {
                this.tacho = tacho;
            }

            public Integer getTagGroups() {
                return tagGroups;
            }

            public void setTagGroups(Integer tagGroups) {
                this.tagGroups = tagGroups;
            }

            public Integer getTags() {
                return tags;
            }

            public void setTags(Integer tags) {
                this.tags = tags;
            }

            public Integer getTollRoads() {
                return tollRoads;
            }

            public void setTollRoads(Integer tollRoads) {
                this.tollRoads = tollRoads;
            }

            public Integer getTrailerGroups() {
                return trailerGroups;
            }

            public void setTrailerGroups(Integer trailerGroups) {
                this.trailerGroups = trailerGroups;
            }

            public Integer getTrailers() {
                return trailers;
            }

            public void setTrailers(Integer trailers) {
                this.trailers = trailers;
            }

            public Integer getUnitCommands() {
                return unitCommands;
            }

            public void setUnitCommands(Integer unitCommands) {
                this.unitCommands = unitCommands;
            }

            public Integer getUnitSensors() {
                return unitSensors;
            }

            public void setUnitSensors(Integer unitSensors) {
                this.unitSensors = unitSensors;
            }

            public Integer getUserNotifications() {
                return userNotifications;
            }

            public void setUserNotifications(Integer userNotifications) {
                this.userNotifications = userNotifications;
            }

            public Integer getVideo() {
                return video;
            }

            public void setVideo(Integer video) {
                this.video = video;
            }

            public Integer getWebResources() {
                return webResources;
            }

            public void setWebResources(Integer webResources) {
                this.webResources = webResources;
            }

            public Integer getWialonActivex() {
                return wialonActivex;
            }

            public void setWialonActivex(Integer wialonActivex) {
                this.wialonActivex = wialonActivex;
            }

            public Integer getWialonMobile2() {
                return wialonMobile2;
            }

            public void setWialonMobile2(Integer wialonMobile2) {
                this.wialonMobile2 = wialonMobile2;
            }

            public Integer getWialonMobileClient() {
                return wialonMobileClient;
            }

            public void setWialonMobileClient(Integer wialonMobileClient) {
                this.wialonMobileClient = wialonMobileClient;
            }

            public Integer getWialonSdk() {
                return wialonSdk;
            }

            public void setWialonSdk(Integer wialonSdk) {
                this.wialonSdk = wialonSdk;
            }

            public Integer getYandexGeocoding() {
                return yandexGeocoding;
            }

            public void setYandexGeocoding(Integer yandexGeocoding) {
                this.yandexGeocoding = yandexGeocoding;
            }

            public Integer getYandexPanorama() {
                return yandexPanorama;
            }

            public void setYandexPanorama(Integer yandexPanorama) {
                this.yandexPanorama = yandexPanorama;
            }

            public Integer getYandexRouting() {
                return yandexRouting;
            }

            public void setYandexRouting(Integer yandexRouting) {
                this.yandexRouting = yandexRouting;
            }

            public Integer getYandexService() {
                return yandexService;
            }

            public void setYandexService(Integer yandexService) {
                this.yandexService = yandexService;
            }

            public Integer getZoneGroups() {
                return zoneGroups;
            }

            public void setZoneGroups(Integer zoneGroups) {
                this.zoneGroups = zoneGroups;
            }

            public Integer getZonesLibrary() {
                return zonesLibrary;
            }

            public void setZonesLibrary(Integer zonesLibrary) {
                this.zonesLibrary = zonesLibrary;
            }

        }
    }
    public static class Ftp {

        @SerializedName("ch")
        @Expose
        private Integer ch;
        @SerializedName("tp")
        @Expose
        private Integer tp;
        @SerializedName("fl")
        @Expose
        private Integer fl;

        public Integer getCh() {
            return ch;
        }

        public void setCh(Integer ch) {
            this.ch = ch;
        }

        public Integer getTp() {
            return tp;
        }

        public void setTp(Integer tp) {
            this.tp = tp;
        }

        public Integer getFl() {
            return fl;
        }

        public void setFl(Integer fl) {
            this.fl = fl;
        }

    }
    public static class Env {

        @SerializedName("max_http_buff")
        @Expose
        private Integer maxHttpBuff;

        public Integer getMaxHttpBuff() {
            return maxHttpBuff;
        }

        public void setMaxHttpBuff(Integer maxHttpBuff) {
            this.maxHttpBuff = maxHttpBuff;
        }

    }
    public static class Classes {

        @SerializedName("avl_hw")
        @Expose
        private Integer avlHw;
        @SerializedName("avl_resource")
        @Expose
        private Integer avlResource;
        @SerializedName("avl_retranslator")
        @Expose
        private Integer avlRetranslator;
        @SerializedName("avl_route")
        @Expose
        private Integer avlRoute;
        @SerializedName("avl_unit")
        @Expose
        private Integer avlUnit;
        @SerializedName("avl_unit_group")
        @Expose
        private Integer avlUnitGroup;
        @SerializedName("user")
        @Expose
        private Integer user;

        public Integer getAvlHw() {
            return avlHw;
        }

        public void setAvlHw(Integer avlHw) {
            this.avlHw = avlHw;
        }

        public Integer getAvlResource() {
            return avlResource;
        }

        public void setAvlResource(Integer avlResource) {
            this.avlResource = avlResource;
        }

        public Integer getAvlRetranslator() {
            return avlRetranslator;
        }

        public void setAvlRetranslator(Integer avlRetranslator) {
            this.avlRetranslator = avlRetranslator;
        }

        public Integer getAvlRoute() {
            return avlRoute;
        }

        public void setAvlRoute(Integer avlRoute) {
            this.avlRoute = avlRoute;
        }

        public Integer getAvlUnit() {
            return avlUnit;
        }

        public void setAvlUnit(Integer avlUnit) {
            this.avlUnit = avlUnit;
        }

        public Integer getAvlUnitGroup() {
            return avlUnitGroup;
        }

        public void setAvlUnitGroup(Integer avlUnitGroup) {
            this.avlUnitGroup = avlUnitGroup;
        }

        public Integer getUser() {
            return user;
        }

        public void setUser(Integer user) {
            this.user = user;
        }

    }
}
