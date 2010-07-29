# This script contains several R commands to do significance tests on the api-location experimental data
# Run commands after runnning loadData.R
# Run each sub-sections (tests) separately, ie donot source this entire document in R

### Statistical tests on Recall

kwDataR <- melt(cR10noNA)
# if you want you can remove outliers based on PR plot
# kwDataRNoOutlier <- subset(kwDataR, !(SCHEME=='S3' & value==1) & !(SCHEME=='B1' & value==1))

# exclude Query 2 and 15 to omit outliers
kwDataRNoOutlier <- melt(cR10noNA[-c(12,7),]) 

# friedman test - nonparametric alternative to a one-way repeated measures analysis of variance
# friedman test on data w/o outliers
friedman.test(as.matrix(cR10noNA[-c(12,7),][,2:6]))
# friedman test on data with outliers
friedman.test(as.matrix(cR10noNA[,2:6]))

# friedman test (asymptotic) with posthoc analysis
# post hoc tests used is: Wilcoxon-Nemenyi-McDonald-Thompson test from Hollander & Wolfe (1999), page 295
# loading the friedman.test.with.post.hoc function from the internet
source("http://www.r-statistics.com/wp-content/uploads/2010/02/Friedman-Test-with-Post-Hoc.r.txt")  
rd <- kwDataR # outliers not removed
### Comparison of five Schemes ("B1", "B2", "S1", "S2", "S3") 
RecallData <- data.frame(
		  Scheme = rd$SCHEME,
		  Query  = rd$QUERY,
		  Recall = rd$value)		  
# boxploting  
with(RecallData , boxplot( Recall  ~ Scheme )) 
# the same with our function. With post hoc, and cool plots
friedman.test.with.post.hoc(Recall ~ Scheme | Query, RecallData)


# test homogenity of variance
bartlett.test(kwDataRNoOutlier$value ~ kwDataRNoOutlier$SCHEME)

# test normality
shapiro.test(kwDataRNoOutlier$value[kwDataRNoOutlier$SCHEME=='B1'])


# anova -- assuming independence
# anova(lm(kwDataR$value ~ kwDataR$SCHEME))
# OR
raov = aov(kwDataR$value ~ kwDataR$SCHEME)

# anova -- for repeated measure within-subject
# two-way anova
# anova(lm(RecallData$Recall~ RecallData$Scheme+ RecallData$Query))
# OR,
raov = aov(lm(RecallData$Recall~ RecallData$Scheme+ RecallData$Query))

summary(raov)
# tukeyHSD (posthoc test)
TukeyHSD(raov)


# kruskal walis test
kruskal.test(kwDataR$value ~ kwDataR$SCHEME)
# kruskal walis test without outliers
kruskal.test(kwDataRNoOutlier$value ~ kwDataRNoOutlier$SCHEME)

#posthoc npmc
library(npmc)
npmcDataR <- kwDataRNoOutlier
colnames(npmcDataR) <- c("QUERY", "var", "class")
summary(npmc(npmcDataR), type="BF")

pairwise.t.test(kwDataRNoOutlier$value, kwDataRNoOutlier$SCHEME, paired=T)

# pairwise wilcox test, needs to be corrected for multiple tests
# wilcox.test(value ~ SCHEME, subset(kwDataR,SCHEME=='B1' | SCHEME=='S3'), paired = T)

# all pair wilcox-test
pairwise.wilcox.test(kwDataRNoOutlier$value, kwDataRNoOutlier$SCHEME, paired=T)
# all pair wilcox-test with outlier
pairwise.wilcox.test(kwDataR$value, kwDataR$SCHEME, paired=T)


### Statistical tests on Precision

kwDataP <- melt(cP10noNA)

shapiro.test(kwDataP$value[kwDataP$SCHEME=='B1'])
shapiro.test(kwDataP$value[kwDataP$SCHEME=='B2'])
shapiro.test(kwDataP$value[kwDataP$SCHEME=='S1'])
shapiro.test(kwDataP$value[kwDataP$SCHEME=='S2'])
shapiro.test(kwDataP$value[kwDataP$SCHEME=='S3'])

friedman.test(as.matrix(cP10noNA[,2:6]))
pd <- kwDataP
### Comparison of five Schemes ("B1", "B2", "S1", "S2", "S3") 
PrecisionData <- data.frame(
		  Precision = pd$value,
		  Scheme = pd$SCHEME,
		  Query  = pd$QUERY)		  
		  
# boxploting  
with(PrecisionData , boxplot( Precision  ~ Scheme )) 
# the same with our function. With post hoc, and cool plots
friedman.test.with.post.hoc(Precision ~ Scheme | Query, PrecisionData)	

# anova
paov = aov(kwDataP$value ~ kwDataP$SCHEME)
summary(paov)

# tukeyHSD (posthoc test)
TukeyHSD(paov)

# for kruskal walis and all pair 
# only 12 queries can be used, since for rest of the queries 
# precision is not defined
# kruskal walis test
kruskal.test(kwDataP$value ~ kwDataP$SCHEME)

# all pair wilcox-test
pairwise.wilcox.test(kwDataP$value, kwDataP$SCHEME, paired=T)

# pairwise wilcox test
# for pairwise more queries can be included if both schemes 
# have a non-NA value for precision
# wilcox.test(value ~ SCHEME, subset(kwDataP,SCHEME=='B1' | SCHEME=='S3'))

### Statistical tests on NDCG

shapiro.test(ndcg$VALUE[ndcg$SCHEME=='B1'])

friedman.test(as.matrix(cNdcgNoNA[,2:6]))
ndd <- melt(cNdcgNoNA) # outliers not removed
### Comparison of five Schemes ("B1", "B2", "S1", "S2", "S3") 
ndData <- data.frame(
		  Scheme = ndd$SCHEME,
		  Query  = ndd$QUERY,
		  NDCG = ndd$value)		  
# boxploting  
with(ndData , boxplot( NDCG  ~ Scheme )) 
# the same with our function. With post hoc, and cool plots
friedman.test.with.post.hoc(NDCG ~ Scheme | Query, ndData)

