package io.servicecomb.serviceregistry.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.notify.NotifyManager;
import io.servicecomb.serviceregistry.notify.RegistryEvent;

public class InstanceVersionCacheManager {

    public static final InstanceVersionCacheManager INSTANCE = new InstanceVersionCacheManager();

    //所有service版本的Instance缓存
    protected Map<String, Map<String, Map<String, MicroserviceInstance>>> cacheAllMap = new ConcurrentHashMap<>();

    //versionrule instance缓存
    protected Map<String, Map<String, Map<String, MicroserviceInstance>>> cacheVRuleMap = new ConcurrentHashMap<>();

    private static final String ALL_VERSION_RULE = "0+";

    private static final String MICROSERVICE_DEFAULT_VERSION = "microservice.default.version";

    private static final Object LOCKOBJECT = new Object();

    private static String getKey(String appId, String microserviceName) {
        if (microserviceName.contains(Const.APP_SERVICE_SEPARATOR)) {
            return microserviceName.replace(Const.APP_SERVICE_SEPARATOR, "/");
        }

        StringBuilder sb = new StringBuilder(appId.length() + microserviceName.length() + 1);
        sb.append(appId).append("/").append(microserviceName);
        return sb.toString();
    }

    private Map<String, MicroserviceInstance> create(String appId, String microserviceName,
            String microserviceVersionRule) {
        List<MicroserviceInstance> instances =
            RegistryUtils.findServiceInstance(appId, microserviceName, microserviceVersionRule);
        if (instances == null) {
            return null;
        }

        Map<String, MicroserviceInstance> instMap = new HashMap<>();
        for (MicroserviceInstance instance : instances) {
            instMap.put(instance.getInstanceId(), instance);
        }
        return instMap;
    }

    public Map<String, MicroserviceInstance> getOrCreateAllMap(String appId, String microserviceName,
            String microserviceVersion) {
        String key = getKey(appId, microserviceName);
        Map<String, Map<String, MicroserviceInstance>> cache = cacheAllMap.get(key);
        if (cache == null) {
            synchronized (LOCKOBJECT) {
                cache = cacheAllMap.get(key);
                if (cache == null) {
                    Map<String, MicroserviceInstance> cacheAllInstance =
                        create(appId, microserviceName, ALL_VERSION_RULE);
                    cache = createCacheVersionMap(cacheAllInstance);
                    cacheAllMap.put(key, cache);
                }
            }
        }
        return cache.get(microserviceVersion);
    }

    public Map<String, Map<String, MicroserviceInstance>> getOrCreateAllMap(String appId, String microserviceName) {
        String key = getKey(appId, microserviceName);
        Map<String, Map<String, MicroserviceInstance>> cache = cacheAllMap.get(key);
        if (cache == null) {
            synchronized (LOCKOBJECT) {
                cache = cacheAllMap.get(key);
                if (cache == null) {
                    Map<String, MicroserviceInstance> cacheAllInstance =
                        create(appId, microserviceName, ALL_VERSION_RULE);
                    cache = createCacheVersionMap(cacheAllInstance);
                    cacheAllMap.put(key, cache);
                }
            }
        }
        return cache;
    }

    private Map<String, Map<String, MicroserviceInstance>> createCacheVersionMap(
            Map<String, MicroserviceInstance> cacheAllInstance) {

        Map<String, Map<String, MicroserviceInstance>> cacheVersionMap =
            new HashMap<String, Map<String, MicroserviceInstance>>();
        for (Map.Entry<String, MicroserviceInstance> ins : cacheAllInstance.entrySet()) {
            String microserviceId = ins.getValue().getServiceId();
            Microservice microservice = RegistryUtils.getMicroservice(microserviceId);
            String version = microservice.getVersion();
            if (version == null || "".equals(version)) {
                version = MICROSERVICE_DEFAULT_VERSION;
            }
            if (cacheVersionMap.get(version) == null) {
                Map<String, MicroserviceInstance> newInsMap = new HashMap<String, MicroserviceInstance>();
                newInsMap.put(ins.getKey(), ins.getValue());
                cacheVersionMap.put(version, newInsMap);
            } else {
                Map<String, MicroserviceInstance> insMap = cacheVersionMap.get(version);
                insMap.put(ins.getKey(), ins.getValue());
            }
        }

        return cacheVersionMap;

    }

    public Map<String, Map<String, MicroserviceInstance>> getOrCreateVRuleMap(String appId, String microserviceName,
            String microserviceVersionRule) {

        String key = getKey(appId, microserviceName);
        Map<String, Map<String, MicroserviceInstance>> cache = cacheVRuleMap.get(key);
        if (cache == null) {
            synchronized (LOCKOBJECT) {
                cache = cacheVRuleMap.get(key);
                if (cache == null) {
                    Map<String, MicroserviceInstance> cacheVersionRuleInstance =
                        create(appId, microserviceName, microserviceVersionRule);
                    cache = createCacheVersionMap(cacheVersionRuleInstance);
                    cacheVRuleMap.put(key, cache);
                }
            }
        }
        return cache;
    }

    public void onInstanceUpdate(MicroserviceInstanceChangedEvent changedEvent) {
        String appId = changedEvent.getKey().getAppId();
        String microserviceName = changedEvent.getKey().getServiceName();
        String version = changedEvent.getKey().getVersion();
        String key = getKey(appId, microserviceName);

        NotifyManager.INSTANCE.notify(RegistryEvent.INSTANCE_CHANGED, changedEvent);

        synchronized (LOCKOBJECT) {

            Map<String, Map<String, MicroserviceInstance>> allCache = cacheAllMap.get(key);
            Map<String, Map<String, MicroserviceInstance>> vRuleCache = cacheAllMap.get(key);
            String instanceId = changedEvent.getInstance().getInstanceId();
            switch (changedEvent.getAction()) {
                case CREATE:
                case UPDATE: 

                    MicroserviceInstance newIns = changedEvent.getInstance();
                    if (allCache != null) {
                        if (allCache.get(version) == null) {
                            Map<String, MicroserviceInstance> newInsMap = new HashMap<String, MicroserviceInstance>();
                            newInsMap.put(instanceId, newIns);
                            allCache.put(version, newInsMap);
                        } else {
                            Map<String, MicroserviceInstance> insMap = allCache.get(version);
                            insMap.put(instanceId, newIns);
                        }
                    }
                    if (vRuleCache != null) {
                        if (vRuleCache.get(version) == null) {
                            Map<String, MicroserviceInstance> newInsMap = new HashMap<String, MicroserviceInstance>();
                            newInsMap.put(instanceId, newIns);
                            vRuleCache.put(version, newInsMap);
                        } else {
                            Map<String, MicroserviceInstance> insMap = vRuleCache.get(version);
                            insMap.put(instanceId, newIns);
                        }
                    }
                    break;
                
                case DELETE:
                    if (allCache != null) {
                        if (allCache.get(version) != null) {
                            Map<String, MicroserviceInstance> insMap = allCache.get(version);
                            insMap.remove(instanceId);
                        }
                    }
                    if (vRuleCache != null) {
                        if (vRuleCache.get(version) != null) {
                            Map<String, MicroserviceInstance> insMap = vRuleCache.get(version);
                            insMap.remove(instanceId);
                        }
                    }
                    break;
                default:
                    return;
            }
        }
    }

    public void cleanUp() {
        synchronized (LOCKOBJECT) {
            cacheVRuleMap.clear();
            cacheAllMap.clear();
        }
    }

}
