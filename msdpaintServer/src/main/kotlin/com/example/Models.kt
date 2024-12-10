package com.example

import org.jetbrains.exposed.dao.id.IntIdTable

/*

ID User DoodleName  Bytes
1  Josh   beans     <data1>
2  Josh   beans     <data2>
3  Josh   beans2    <data3>

*/


object Doodle : IntIdTable() {

    val name = varchar("name", 100).uniqueIndex()
    val user = reference("user_id", User.id)
    val timestamp = long("timestamp")
    val data = text("data")

}

object User : IntIdTable() {

    val email = varchar("email", 100).uniqueIndex()
    //val name = varchar("name", 50)

}

object Shared : IntIdTable() {

    val user1 = reference("user1_id", User.id)
    val user2 = reference("user2_id", User.id)
    val doodle = reference("doodle_id", Doodle.id)

}


