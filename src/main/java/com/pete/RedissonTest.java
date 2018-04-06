package com.pete;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class RedissonTest {

  public static void main(String[] args) {

    Config config = new Config();
    config.useSingleServer()
        .setAddress("redis://localhost:6379");

    RedissonClient redissonClient = Redisson.create(config);

    //Write a String of invalid JSON
    redissonClient.getBucket("testbucket", new StringCodec()).set("{INVALID JSON!}");

    while (true) {
      try {
        //Attempt to read the invalid value as JSON
        redissonClient.getBucket("testbucket", new JsonJacksonCodec()).get();
      } catch (RedisTimeoutException e) {
        //After a few tries RedisTimeoutException will be thrown
        break;
      } catch (RedisException e) {
        //First few tries will fail with this exception
        System.out.println("Failed to read: " + e.getMessage());
      }
    }

    //At this point, Redisson is unable to do anything - all connections depleted
    redissonClient.getBucket("testbucket2").set("should work");
  }
}
