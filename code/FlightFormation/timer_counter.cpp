#include <iostream>
#include "timer_counter.h"

using namespace std;

bool timer_counter::instanceFlag = false;
timer_counter* timer_counter::single = NULL;
timer_counter* timer_counter::getInstance()
{
    if(! instanceFlag)
    {
        single = new timer_counter();
        instanceFlag = true;
        return single;
    }
    else
    {
        return single;
    }
}

void timer_counter::output()
{
    cout << "[STATS] SYNC:" << sync_mode << " MAINTAIN: ";
    cout << maintain_mode << " CA:" << maintain_mode;
}

void timer_counter::inc_sync(){
    this->end = time(NULL);
    this->sync_mode += (this->end-this->start);
    this->start = this->end;
    this->output();
};
void timer_counter::inc_maintain()
{
    this->end = time(NULL);
    this->maintain_mode += (this->end-this->start);
    this->start = this->end;
    this->output();
};
void timer_counter::inc_avoidance()
{
    this->end = time(NULL);
    this->avoidance_mode += (this->end-this->start);
    this->start = this->end;
    this->output();
};