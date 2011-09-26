/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domain.scm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.SpecialAccessControlHandler;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.common.util.SecurityUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class FilePathAccessControlHandler implements SpecialAccessControlHandler {

    private UserDataManager userManager;

    private List<SecurityAttributeProvider> attributeProviders;

    @Override
    public boolean isAuthorized(String username, final MethodInvocation invocation) {
        Collection<RepositoryFilePermission> permissions =
            userManager.getAllPermissionsForUser(username, RepositoryFilePermission.class);
        final Object[] arguments = invocation.getArguments();
        List<Integer> pathsIndexes = getPathParams(invocation.getMethod());
        List<String> pathValues = Lists.transform(pathsIndexes, new Function<Integer, String>() {
            @Override
            public String apply(Integer input) {
                return (String) arguments[input];
            }
        });

        Collection<SecurityAttributeEntry> securityAttributes =
            SecurityUtils.getSecurityAttributesForMethod(invocation.getMethod());
        final Collection<String> operations = new ArrayList<String>();
        for (SecurityAttributeEntry a : securityAttributes) {
            if ("file.operation".equals(a.getKey())) {
                operations.add(a.getValue());
            }
        }

        for (final String pathValue : pathValues) {
            boolean allowed = Iterators.any(permissions.iterator(), new Predicate<RepositoryFilePermission>() {
                @Override
                public boolean apply(final RepositoryFilePermission perm) {
                    String path = perm.getPath();
                    if (path != null && !pathValue.startsWith(path)) {
                        return false;
                    }
                    if (perm.getContext() != null &&
                            !perm.getContext().equals(ContextHolder.get().getCurrentContextId())) {
                        return false;
                    }
                    if (perm.getInstance() != null) {
                        boolean hasInstance =
                            Iterators.any(attributeProviders.iterator(), new Predicate<SecurityAttributeProvider>() {
                                @Override
                                public boolean apply(SecurityAttributeProvider input) {
                                    Collection<String> instanceIds =
                                        SecurityUtils.getServiceInstanceIds(attributeProviders, invocation.getThis());
                                    return instanceIds.contains(perm.getInstance());
                                }
                            });
                        if (!hasInstance) {
                            return false;
                        }
                    }
                    if (perm.getOperation() != null) {
                        return operations.contains(perm.getOperation());
                    }
                    return true;
                }
            });
            if (!allowed) {
                return false;
            }
        }
        return true;

    }

    private List<Integer> getPathParams(Method method) {
        List<Integer> result = Lists.newArrayList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation a : annotations) {

                Class<? extends Annotation> annotationType = a.annotationType();
                Class<PathParameter> paramAnnotationType = PathParameter.class;
                ClassLoader classLoader = annotationType.getClassLoader();
                ClassLoader classLoader2 = paramAnnotationType.getClassLoader();

                if (annotationType.equals(paramAnnotationType)) {
                    result.add(i);
                }
            }
        }
        return result;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

    public void setAttributeProviders(List<SecurityAttributeProvider> attributeProviders) {
        this.attributeProviders = attributeProviders;
    }

}
