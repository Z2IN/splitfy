package org.zzin.splitfy.domain.auth.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;

@Service
@RequiredArgsConstructor
public class DefaultAuthInnerService implements AuthInnerService {

  private final AuthRepository authRepository;

  @Override
  public Map<Long, String> findByIdIn(Set<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    return authRepository.findByIdIn(userIds).stream()
        .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
  }
}
