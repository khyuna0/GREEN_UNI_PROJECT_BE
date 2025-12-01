package com.green.university.repository.interfaces;

import com.green.university.dto.RoomFormDto;
import com.green.university.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  강의실 repository
 */


public interface RoomRepository extends JpaRepository<Room,Long> {

}
