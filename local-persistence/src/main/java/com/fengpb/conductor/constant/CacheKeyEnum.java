package com.fengpb.conductor.constant;

import java.util.concurrent.TimeUnit;

public enum CacheKeyEnum {
    local_task_cache() {
        @Override
        public long expireTime() {
            return 5;
        }

        @Override
        public TimeUnit timeUnit() {
            return TimeUnit.SECONDS;
        }

        @Override
        public long maximumSize() {
            return 100L;
        }
    },
    local_workflow_cache() {
        @Override
        public long expireTime() {
            return 5;
        }

        @Override
        public TimeUnit timeUnit() {
            return TimeUnit.SECONDS;
        }

        @Override
        public long maximumSize() {
            return 100L;
        }

    },
    local_workflow_def_cache() {
        @Override
        public String key(Object... values) {
            return String.format("local_workflow_def_cache", values);
        }
        @Override
        public long expireTime() {
            return 1;
        }

        @Override
        public TimeUnit timeUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        public long maximumSize() {
            return 100L;
        }
    },
    local_workflow_task_cache() {
        @Override
        public long expireTime() {
            return 5;
        }

        @Override
        public TimeUnit timeUnit() {
            return TimeUnit.SECONDS;
        }

        @Override
        public long maximumSize() {
            return 100L;
        }
    }
    ;
    public String key(Object... values) {
        throw new AbstractMethodError();
    }

    public long expireTime() {
        throw new AbstractMethodError();
    }

    public long waitTime() {
        throw new AbstractMethodError();
    }

    public TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }

    public long maximumSize() {
        throw new AbstractMethodError();
    }
}
