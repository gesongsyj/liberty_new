#sql("paginate")
	select *
	from kline
	#set(flag=0)
	#if(qo.keyword)
		#(flag==0?"where":"and") id like concat('%',#para(qo.keyword),'%')
		#set(flag=1)
	#end
#end

#sql("getLastOneByCode")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code=#para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type=#para(type)
		#set(flag=1)
	#end
	order by k.date desc
#end

#sql("getLastOneByCodeAndDate")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code=#para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type=#para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") k.date <=#para(date)
		#set(flag=1)
	#end
	order by k.date desc
#end

#sql("getLast2ByCode")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code=#para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type=#para(type)
		#set(flag=1)
	#end
	order by k.date desc
	limit 0,2
#end

#sql("getLast2ByCurrencyId")
	select *
	from kline
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId=#para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type=#para(type)
		#set(flag=1)
	#end
	order by date desc
	limit 0,2
#end

#sql("listAllByCode")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("listAllByCodeBeforeDate")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") k.date <= #para(date)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("getListAfterDate")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(date)
		#(flag==0?"where":"and") k.date >= #para(date)
		#set(flag=1)
	#end
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("getByDateRange")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(startDate)
		#(flag==0?"where":"and") k.date >= #para(startDate)
		#set(flag=1)
	#end
	#if(endDate)
		#(flag==0?"where":"and") k.date <= #para(endDate)
		#set(flag=1)
	#end
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("getByCurrencyId")
	select *
	from kline 
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId >= #para(currencyId)
		#set(flag=1)
	#end
#end
#sql("listBeforeDate")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") k.date <= #para(date)
		#set(flag=1)
	#end
	order by date desc limit #para(limit)
#end
#sql("deleteByCurrencyId")
    delete from kline where currencyId = #para(currencyId)
#end
#sql("getByDate")
	select k.*
	from currency c STRAIGHT_JOIN kline k on k.currencyId=c.id
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") c.code = #para(code)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") k.type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") k.date = #para(date)
		#set(flag=1)
	#end
#end
