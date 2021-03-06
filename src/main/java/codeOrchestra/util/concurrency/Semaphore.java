/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package codeOrchestra.util.concurrency;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Semaphore {
  private static final class Sync extends AbstractQueuedSynchronizer {
    public int tryAcquireShared(int acquires) {
      return getState() == 0 ? 1 : -1;
    }

    public boolean tryReleaseShared(int releases) {
      // Decrement count; signal when transition to zero
      while (true) {
        int c = getState();
        if (c == 0) return false;
        int nextc = c - 1;
        if (compareAndSetState(c, nextc)) return nextc == 0;
      }
    }

    final void down() {
      while (true) {
        int current = getState();
        int next = current + 1;
        if (compareAndSetState(current, next)) return;
      }
    }
  }

  private final Sync sync = new Sync();

  public void up() {
    tryUp();
  }

  public boolean tryUp() {
    return sync.releaseShared(1);
  }

  public void down() {
    sync.down();
  }

  public void waitFor() {
    try {
      waitForUnsafe();
    }
    catch (InterruptedException e) {
      throw new ProcessCanceledException(e);
    }
  }

  public void waitForUnsafe() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
  }

}