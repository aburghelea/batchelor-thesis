CC=g++
CFLAGS= -Wall -g
LDFLAGS = -lboost_system -lboost_thread -lpthread -lboost_program_options -lboost_regex -lboost_timer -lboost_date_time -ljson_spirit 


all: ai_manager

ai_manager: formation.o ai_manager.o uav_position.o position_container.o timer_counter.o
	$(CC) $(CFLAGS) -o $@ $^  $(LDFLAGS)
	
formation: formation.o
	$(CC) $(CFLAGS) -o $@ $^  $(LDFLAGS)

uav_position.o: uav_position.cpp uav_position.h

formation.o: formation.cpp formation.h

ai_manager.o: ai_manager.cpp

position_container.o: position_container.cpp position_container.h

timer_counter.o: timer_counter.cpp timer_counter.h

clean-temp:
	rm -rf *.o *.obj
clean: clean-temp
	rm -rf  ai_manager read_formation
