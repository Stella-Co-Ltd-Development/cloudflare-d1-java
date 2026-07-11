package io.github.xxvw.cloudflare.d1.testsupport;

import java.util.Objects;

/**
 * Shared row type for typed query mapping tests.
 */
public final class UserRow {
  public long id;
  public String name;

  public UserRow() {}

  public UserRow(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserRow)) {
      return false;
    }
    UserRow that = (UserRow) other;
    return id == that.id && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    return "UserRow{id=" + id + ", name='" + name + "'}";
  }
}
