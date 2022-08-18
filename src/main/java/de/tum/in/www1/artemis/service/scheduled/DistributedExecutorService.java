package de.tum.in.www1.artemis.service.scheduled;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.stereotype.Service;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MemberSelector;
import com.hazelcast.core.HazelcastInstance;

@Service
public class DistributedExecutorService {

    private final Logger log = LoggerFactory.getLogger(DistributedExecutorService.class);

    private final HazelcastInstance hazelcastInstance;

    private final DiscoveryClient discoveryClient;

    private final Optional<Registration> registration;

    public DistributedExecutorService(HazelcastInstance hazelcastInstance, DiscoveryClient discoveryClient, Optional<Registration> registration) {
        this.hazelcastInstance = hazelcastInstance;
        this.discoveryClient = discoveryClient;
        this.registration = registration;
    }

    public <T> Future<T> executeTaskOnMemberWithProfile(Callable<T> taskCallable, String profile) {
        if (this.registration.isEmpty()) {
            // No distributed setup -> This instance has to execute it
            // TODO
            return null;
        }

        var instances = discoveryClient.getInstances(registration.get().getServiceId());
        var instancesWithMatchingProfile = instances.stream().filter(instance -> Arrays.asList(instance.getMetadata().getOrDefault("profile", "").split(",")).contains(profile))
                .toList();

        log.info("Instances with profile {} are {}", profile, instancesWithMatchingProfile.stream().map(ServiceInstance::getInstanceId).collect(Collectors.toList()));

        var hazelcastInstancesWithMatchingProfile = hazelcastInstance.getCluster().getMembers().stream()
                .filter(member -> Arrays.asList(member.getAttributes().getOrDefault("profile", "").split(",")).contains(profile)).toList();

        log.info("Hazelcast Instances with profile {} are {}", profile, hazelcastInstancesWithMatchingProfile.stream().map(Member::getAddress).collect(Collectors.toList()));

        log.info("Hazelcast members are {}", hazelcastInstance.getCluster().getMembers().stream()
                .map(m -> "Address: %s, Attributes %s".formatted(m.getAddress(), m.getAttributes().toString())).collect(Collectors.toList()));

        return hazelcastInstance.getExecutorService("test").submit(taskCallable, new ProfileMemberSelector(profile));
    }

    static class ProfileMemberSelector implements MemberSelector {

        String profile;

        ProfileMemberSelector(String profile) {
            this.profile = profile;
        }

        @Override
        public boolean select(Member member) {
            return Arrays.asList(member.getAttributes().getOrDefault("profiles", "").split(",")).contains(profile);
        }
    }

}
