library(reshape)
eval <- read.table("./galagoOP",header=TRUE,sep=",")

ndcg <- subset(eval, (METRIC == "ndcg" & QUERY != "all"), select=c("QUERY","VALUE","SCHEME"))
mNdcg <- melt(ndcg)
cNdcg <- cast(mNdcg, QUERY ~ SCHEME)
cNdcgNoNA <- cNdcg[-c(3,5,8,10, 15,16,18,20),]
 
R10 <- subset(eval, (METRIC == "R10" & QUERY != "all"), select=c("QUERY","VALUE","SCHEME"))
mR10 <- melt(R10)
cR10 <- cast(mR10, QUERY ~ SCHEME)

cR10noNA <- cR10
# replace NA with 0, NA is there due to the fact that there are no entries for schemes that 
# do not produce any result for a query. For such cases recall is zero.
cR10noNA[is.na(cR10noNA)] <- 0
 
P10 <- subset(eval, (METRIC == "P10" & QUERY != "all"), select=c("QUERY","VALUE","SCHEME"))
mP10 <- melt(P10)
cP10 <- cast(mP10, QUERY ~ SCHEME)

# remove rows with NA for comparing Precision
# for analysis of variance, we remove queries whose precision is not
# defined under a scheme
cP10noNA <- cP10[-c(3,5,8,10, 15,16,18,20),] 