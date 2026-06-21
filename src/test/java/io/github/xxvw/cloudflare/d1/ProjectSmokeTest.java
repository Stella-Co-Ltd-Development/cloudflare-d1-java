package io.github.xxvw.cloudflare.d1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProjectSmokeTest {

  @Test
  void publicPackageIsAvailable() throws ClassNotFoundException {
    Package publicPackage = Class.forName("io.github.xxvw.cloudflare.d1.package-info").getPackage();

    assertThat(publicPackage.getName()).isEqualTo("io.github.xxvw.cloudflare.d1");
  }
}
