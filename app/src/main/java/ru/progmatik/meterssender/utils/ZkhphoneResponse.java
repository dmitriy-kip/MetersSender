package ru.progmatik.meterssender.utils;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZkhphoneResponse {

    private ZkhphoneResponseResult result = null;

    public ZkhphoneResponseResult getResult() {
        return this.result;
    }
    public void createResult() {
        this.result = new ZkhphoneResponseResult();
    }

    public class ZkhphoneResponseResult {

        private Integer code = 0;
        private String desc = "";
        private String _session = "";
        private zkhphoneResponseResultList list = new zkhphoneResponseResultList();

        public void set_session(String _session) {
            this._session = _session;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String get_session() {
            return _session;
        }

        public Integer getCode() {
            return this.code;
        }

        public String getDesc() {
            return this.desc;
        }

        public zkhphoneResponseResultList getList() {
            return this.list;
        }
        public void createList() { this.list = new zkhphoneResponseResultList(); }

        public class zkhphoneResponseResultList {

            private String type = "";
            private ArrayMap<String, String> attributes = new ArrayMap<>();
            private List<ArrayMap<String, String>> items = new ArrayList<>();


            public void setType(String type ) {
                this.type = type;
            }

            public String getType() {
                return this.type;
            }

            public void addItem(ArrayMap<String, String> it) {
                this.items.add(it);
            }

            public List<ArrayMap<String, String>> getItems() {
                return this.items;
            }

            public ArrayMap<String, String> getAttributes() {
                return this.attributes;
            }
        }
    }
}
