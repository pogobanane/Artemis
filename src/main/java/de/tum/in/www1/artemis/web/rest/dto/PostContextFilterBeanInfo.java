package de.tum.in.www1.artemis.web.rest.dto;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class PostContextFilterBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(PostContextFilter.class, null);
    }
}
