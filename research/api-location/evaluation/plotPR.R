# run this after runnning loadData.R

countNonZero <- function(arow){
	length(which(arow > 0))/20
	}
	
countNonNA <- function(arow){
	length(which(! is.na(arow)))/20
	}


plotTable <- function(tcast, tabtitle, tabsubtitle){
	numQ <- nrow(tcast)
	tflat <- melt(tcast)
	#tflat
	xbar <- tapply(tflat$value, tflat$SCHEME, mean, na.rm=TRUE)
	
	#s <- tapply(tflat$value, tflat$SCHEME, sd, na.rm=TRUE)
	#n <- tapply(tflat$value, tflat$SCHEME, length)
	#sem <- s/sqrt(n)
	
	
	boxplot(tcast[2:6], col="grey90", cex.axis=2, cex.label=2, cex=2, pch=4, border="grey30")
	title(main = tabtitle, sub = tabsubtitle, cex.main=2, cex.sub = 0.75, font.sub = 3)
	for(i in 2:6){
 		points(jitter( rep(i-1, each=numQ), 2.5), 
			tcast[[i]], cex=1, pch=1, col="grey30")
	}
	# stripchart(tflat$value ~ tflat$variable,method="jitter", jit=0.05, pch=16, vert=T)
	# arrows(1:5,xbar + (s),1:5,xbar-(s),angle=90,code=3,length=.1)
	lines(1:5,xbar,pch=18,type="b",cex=2, lwd=1.5)
	
}

plotAvg <- function(){
	
	all <- subset(eval, (QUERY=="all" & (METRIC == "map" | METRIC == "ndcg" | METRIC == "bpref")), select=c("METRIC","VALUE","SCHEME"))

	all$VALUE <- as.numeric(all$VALUE) 
	nmeasures <- 3

	# get the range for the x and y axis 
	xrange <- range(c(1:5))
	yrange <- range(c(0:1)) 

	# set up the plot 
	plot(xrange, yrange, type="n", xlab="Schemes", ylab="Metric Value" ,cex.axis=2, cex=2, axisnames = FALSE, axes = FALSE) 
	# axis(1, at=1:5, labels=c("B    ", "B    ","S    ","S    ","S    "), cex.axis=2) # cheating by overlaying characters
	colors <- rainbow(nmeasures) 
	linetype <- c(1:nmeasures) 
	plotchar <- seq(18, 18 + nmeasures + 1, 1)

	# add lines 
	for (i in 1:nmeasures) { 
		measure <- subset(all, all$METRIC == all$METRIC[i]) 
		lines(1:5,measure$VALUE, type="b", lwd=1.5, lty=linetype[i], col=colors[i], pch=plotchar[i], cex=2) 
	} 	
	
	tflat <- melt(cR10noNA)
	countBar <- tapply(tflat$value, tflat$SCHEME, countNonZero)
	lines(1:5,countBar,pch=21,type="b",cex=2.2)

	# add a title and subtitle 
	title("Perfomance Measures (Avg.)", cex.main=2)

	# add a legend 
	legend(xrange[1], yrange[2], all$METRIC[1:nmeasures], cex=1.4, col=colors, pch=plotchar, lty=linetype, title="Metric")
	
}



par(mfrow=c(1,3), oma=c(4,4,4,4), mar=c(2,2,1.8,1))

plotTable(cP10noNA,"Precision", "(12 queries)")
plotTable(cR10noNA, "Recall", "(20 queries)")
plotTable(cNdcg, "NDCG", "(20 queries)")

# plotAvg()
 
 
 
# boxplot(R10$VALUE ~ R10$SCHEME)
 
 