package org.apache.servicecomb.scaffold.edge;

import org.apache.servicecomb.scaffold.edge.darklaunch.DarkLaunchRule;

//保存灰度发布动态配置，避免JSON反序列化开销
public class DynamicDarkLaunchSetting {
  private final String config;

  private final DarkLaunchRule rule;

  public String getConfig() {
    return config;
  }

  public DarkLaunchRule getRule() {
    return rule;
  }

  public DynamicDarkLaunchSetting(String config, DarkLaunchRule rule) {
    this.config = config;
    this.rule = rule;
  }
}