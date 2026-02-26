package com.orangefrc.annotation;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

public class Update {
    
    public class DashboardContainer<T>{
        private Name key;
        private TypeMirror type;
        private T val;
        

        public DashboardContainer(Name key, TypeMirror type, T val) {
            this.key = key;
            this.type = type;
            this.val = val;
        }

        public Name getKey() {
            return key;
        }

        public TypeMirror getType() {
            return type;
        }

        public String putDashboardMethod() {
            return "SmartDashboard.put" + type.toString() + "(" + key +", " + val + ")";
        }

    }


    public static boolean checkForUpdate() {
        return true;
    }

    public static void updateJson() {

    }
}
