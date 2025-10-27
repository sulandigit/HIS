package com.neu.his.cloud.service.sms.constant;

/**
 * Redis缓存Key常量类
 * 统一管理所有Redis缓存的Key命名
 * 命名规范：{系统前缀}:{业务模块}:{数据类型}:{唯一标识}[:{子标识}]
 */
public class RedisKeyConstants {

    /**
     * 系统前缀
     */
    public static final String SYSTEM_PREFIX = "hospital";

    /**
     * 业务模块前缀
     */
    public static class Module {
        /** 系统管理模块 */
        public static final String SMS = "sms";
        /** 诊疗管理模块 */
        public static final String DMS = "dms";
        /** 患者管理模块 */
        public static final String PMS = "pms";
        /** 收费管理模块 */
        public static final String BMS = "bms";
    }

    /**
     * SMS模块 - 科室相关缓存Key
     */
    public static class Dept {
        /** 全部科室信息 */
        public static final String ALL = SYSTEM_PREFIX + ":" + Module.SMS + ":dept:all";
    }

    /**
     * SMS模块 - 挂号级别相关缓存Key
     */
    public static class RegistrationRank {
        /** 全部挂号级别信息 */
        public static final String ALL = SYSTEM_PREFIX + ":" + Module.SMS + ":registration_rank:all";
    }

    /**
     * SMS模块 - 员工相关缓存Key
     */
    public static class Staff {
        /** 全部员工信息 */
        public static final String ALL = SYSTEM_PREFIX + ":" + Module.SMS + ":staff:all";
    }

    /**
     * DMS模块 - 非药品相关缓存Key
     */
    public static class NonDrug {
        /** 全部非药品信息 */
        public static final String ALL = SYSTEM_PREFIX + ":" + Module.DMS + ":nondrug:all";
    }

    /**
     * DMS模块 - 病历草稿相关缓存Key
     * 使用方法：String.format(RedisKeyConstants.CaseDraft.KEY_TEMPLATE, registrationId)
     */
    public static class CaseDraft {
        /** 病历草稿Key模板，参数：挂号ID */
        public static final String KEY_TEMPLATE = SYSTEM_PREFIX + ":" + Module.DMS + ":case:draft:%d";
        
        /**
         * 生成病历草稿Key
         * @param registrationId 挂号ID
         * @return 完整的缓存Key
         */
        public static String getKey(Long registrationId) {
            return String.format(KEY_TEMPLATE, registrationId);
        }
    }

    /**
     * DMS模块 - 药品处方暂存相关缓存Key
     * 使用方法：String.format(RedisKeyConstants.Prescription.KEY_TEMPLATE, registrationId, type)
     */
    public static class Prescription {
        /** 药品处方暂存Key模板，参数：挂号ID, 类型(4-成药/5-草药) */
        public static final String KEY_TEMPLATE = SYSTEM_PREFIX + ":" + Module.DMS + ":prescription:temp:%d:%d";
        
        /**
         * 生成药品处方暂存Key
         * @param registrationId 挂号ID
         * @param type 类型(4-成药/5-草药)
         * @return 完整的缓存Key
         */
        public static String getKey(Long registrationId, int type) {
            return String.format(KEY_TEMPLATE, registrationId, type);
        }
    }

    /**
     * DMS模块 - 非药品项目暂存相关缓存Key
     * 使用方法：String.format(RedisKeyConstants.NonDrugTemp.KEY_TEMPLATE, registrationId, type)
     */
    public static class NonDrugTemp {
        /** 非药品项目暂存Key模板，参数：挂号ID, 类型(0-检查/1-检验/2-处置) */
        public static final String KEY_TEMPLATE = SYSTEM_PREFIX + ":" + Module.DMS + ":nondrug:temp:%d:%d";
        
        /**
         * 生成非药品项目暂存Key
         * @param registrationId 挂号ID
         * @param type 类型(0-检查/1-检验/2-处置)
         * @return 完整的缓存Key
         */
        public static String getKey(Long registrationId, int type) {
            return String.format(KEY_TEMPLATE, registrationId, type);
        }
    }

    /**
     * 缓存过期时间常量（单位：秒）
     */
    public static class ExpireTime {
        /** 基础数据过期时间：24小时 */
        public static final long BASE_DATA = 24 * 60 * 60;
        /** 业务数据过期时间：2小时 */
        public static final long BUSINESS_DATA = 2 * 60 * 60;
        /** 临时草稿过期时间：30分钟 */
        public static final long TEMP_DRAFT = 30 * 60;
    }
}
