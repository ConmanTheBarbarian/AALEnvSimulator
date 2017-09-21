library(RMySQL)
library(zoo)
con <- dbConnect(RMySQL::MySQL(),dbname="simtest",user="access",password="accessOnly")



gdb <- function(con,x) {
	res <- dbSendQuery(con,paste0("select distinct result.t,virtualSubject.name,resultParameter.name,resultParameter.value FROM virtualSubject inner join result on virtualSubject.id=result.vid left join resultParameter on resultParameter.rid=result.id where virtualSubject.name='",x,"' and resultParameter.name='Delta outlier ratio' order by result.t"));
	l<-dbFetch(res)
	dbClearResult(res)
	return(l)
}

zooify <- function(x) {
	return(zoo(x[["value"]]))
}

a11<-gdb(con,"1-1")
z11<-zooify(a11)
title("X")
plot(z11[14:length(z11)],xlab="Time index",ylab="Anomaly index")

mypl <- function(con,x) {
	rws<-gdb(con,x);
	zrows<-zooify(rws);
	dev.new()
	plot(zrows[14:length(zrows)],xlab="Time index",ylab="Anomaly index",main=x)
}
mypl(con,"1-1")
mypl(con,"1-2")
mypl(con,"1-3")
mypl(con,"1-4")
mypl(con,"1-5")
mypl(con,"1-6")
mypl(con,"1-1-2")
mypl(con,"1-2-2")
mypl(con,"1-3-2")
mypl(con,"1-4-2")
mypl(con,"1-5-2")

ex<-c("1-1","1-2","1-3","1-4","1-5","1-6")
dim(ex)<-6
exf<-apply(ex,1,function(x){ zooify(gdb(con,x)) })
exc<-cbind(exf[[1]],exf[[2]],exf[[3]],exf[[4]],exf[[5]],exf[[6]])
exca<-apply(exc,1,mean)
excs<-apply(exc,1,sd)
ex1<-cbind(exc
plot(exca,xlab="Time index",ylab="Average anomaly indicator", main="Average anomaly indicator, no disruption")
plot(excs,xlab="Time index",ylab="Longitudinal standard deviation of anomaly indicator", main="Standard deviation of anomaly indicator, no disruption")

