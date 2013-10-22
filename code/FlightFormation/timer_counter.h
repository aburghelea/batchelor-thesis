#pragma once

#include <time.h>

class timer_counter {
private:
    static bool instanceFlag;
    static timer_counter *single;
    double sync_mode;
    double maintain_mode;
    double avoidance_mode;

    time_t start;
    time_t end;

    timer_counter() :
        sync_mode ( 0 ),
        maintain_mode ( 0 ),
        avoidance_mode ( 0 ),
        start ( time ( NULL ) ) {

    }
public:
    static timer_counter* getInstance();
    void output();
    ~timer_counter() {
        instanceFlag = false;
    }
    void inc_sync();
    void inc_maintain();
    void inc_avoidance();

};
// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
