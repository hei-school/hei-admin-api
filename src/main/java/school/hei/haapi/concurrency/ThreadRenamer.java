package school.hei.haapi.concurrency;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Slf4j
public class ThreadRenamer {
  public static void renameWorkerThread(Thread thread) {
    String name = "w-" + randomUUID();
    renameThread(thread, name);
  }

  private static String randomUUID() {
    return UUID.randomUUID().toString().substring(0, 6);
  }

  public static void renameFrontalThread(Thread thread) {
    String name = "f-" + randomUUID();
    renameThread(thread, name);
  }

  public static void renameThread(Thread thread, String newName) {
    log.info("renaming {}#{} thread to {}", thread.getName(), thread.threadId(), newName);
    thread.setName(newName);
  }

  public static String getRandomSubThreadNamePrefixFrom(Thread parent) {
    return parent.getName() + "-" + randomUUID();
  }
}
