package org.zzin.splitfy.domain.auth.service;

import java.util.Map;
import java.util.Set;

public interface AuthInnerService {

  Map<Long, String> findByIdIn(Set<Long> userIds);
}
