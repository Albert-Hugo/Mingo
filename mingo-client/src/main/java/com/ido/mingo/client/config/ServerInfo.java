package com.ido.mingo.client.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @Author Ido
 * @Description the target server that client need to connect
 * @Date 12:17 2020/7/27
 **/
@Getter
@AllArgsConstructor
public class ServerInfo {
    final private String host;
    final private int port;

}
