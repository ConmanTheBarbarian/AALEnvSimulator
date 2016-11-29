create database simtest;
use simtest;
drop table intData;
drop table doubleData;
drop table log ;
drop table resultParameter;
drop table result;

drop table eventType;
drop table virtualSubjectConfiguration;
drop table virtualSubject;
drop database simtest; 

/*table creation section */

create table virtualSubject(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(128),
UNIQUE INDEX name_index(name)
) DATA DIRECTORY = 'D:\DBS';

create table virtualSubjectConfiguration(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
vid INTEGER NOT NULL,
INDEX vid_index(vid),
FOREIGN KEY(vid)
  references virtualSubject(id)
  on delete cascade,
  name VARCHAR(128),
  value DOUBLE) DATA DIRECTORY = 'D:\DBS';


create table eventType(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(128),
UNIQUE INDEX eventType_index(name),
details VARCHAR(256)) DATA DIRECTORY = 'D:\DBS';

create table log(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
t timestamp NOT NULL,
eid INTEGER NOT NULL,
UNIQUE INDEX eidTime_index(vid,eid,t),
INDEX eid_index(eid),
FOREIGN KEY(eid)
	references eventType(id)
    on delete cascade,
vid INTEGER NOT NULL,
INDEX vid_index(vid),
FOREIGN KEY(vid)
    references virtualSubject(id)
    on delete cascade,
information VARCHAR(256)
) DATA DIRECTORY = 'D:\DBS';

create table doubleData(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
lid INTEGER NOT NULL,
INDEX lid_index(lid),
foreign key(lid) 
	references log(id)
    on delete cascade,
name VARCHAR(32),
INDEX doubleData_index(name),
UNIQUE INDEX lidAndName_index(lid,name),
data DOUBLE) DATA DIRECTORY= 'D:\DBS';

create table intData(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
lid INTEGER NOT NULL,
INDEX lid_index(lid),
foreign key(lid) 
	references log(id)
    on delete cascade,
name VARCHAR(32),
INDEX intData_index(name),
UNIQUE INDEX lidAndName_index(lid,name),
data INTEGER) DATA DIRECTORY= 'D:\DBS';

create table result(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
vid INTEGER NOT NULL,
INDEX vid_index(vid),
foreign key(vid)
  references virtualSubject(id)
  on delete cascade,
t TIMESTAMP NOT NULL,
commentText VARCHAR(128)
) DATA DIRECTORY = 'D:\DBS';

create table resultParameter(
id INTEGER PRIMARY KEY AUTO_INCREMENT,
rid INTEGER NOT NULL,
INDEX rid_index(rid),
foreign key(rid)
  references result(id)
  on delete cascade,
 name VARCHAR(32),
 value DOUBLE) DATA DIRECTORY = 'D:\DBS';

/* grant section */
grant select, insert, update, delete on eventType to access@localhost;
grant select, insert, update, delete on log to access@localhost;
grant select, insert, update, delete on doubleData to access@localhost;
grant select, insert, update, delete on intData to access@localhost;
grant select, insert, update, delete on  virtualSubject to access@localhost;
grant select, insert, update, delete on  virtualSubjectConfiguration to access@localhost;
grant select, insert, update, delete on  result to access@localhost;
grant select, insert, update, delete on  resultParameter to access@localhost;

/* query example section */
select * from log;
select log.t,log.vid,eventtype.name,eventType.details,doubleData.name,doubleData.data from log inner join eventtype on log.eid=eventtype.id left join doubleData on log.id=doubleData.lid WHERE virtualsubject.name="1-2" order by log.t;
select * from eventType;
describe eventtype;
select * from doubleData;
delete from log where vid=(SELECT id FROM virtualSubject WHERE name='1-2');
delete from virtualSubject where name='1-2' LIMIT 1000000;
desc tablespace;
select * from virtualSubject;
select virtualSubject.name,result.id,result.t,resultParameter.name,resultParameter.value FROM virtualSubject inner join result on virtualSubject.id=result.vid left join resultParameter on resultParameter.rid=result.id where virtualSubject.name="1-4" order by result.t;
select * from result;
select * from resultParameter;
delete from result WHERE id >0 and vid=(SELECT id FROM virtualSubject WHERE name="1-5") LIMIT 1000000;
