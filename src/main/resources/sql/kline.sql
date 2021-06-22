#sql("paginate")
	select *
	from kline
	#set(flag=0)
	#if(qo.keyword)
		#(flag==0?"where":"and") id like concat('%',#para(qo.keyword),'%')
		#set(flag=1)
	#end
#end

#sql("getLastOneByCurrencyId")
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
#end

#sql("getLastOneByCurrencyIdAndDate")
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
	#if(date)
		#(flag==0?"where":"and") date <=#para(date)
		#set(flag=1)
	#end
	order by date desc
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

#sql("listAllByCurrencyId")
	select *
	from kline
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("listAllByCurrencyIdBeforeDate")
	select *
	from kline
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") date <= #para(date)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("getListAfterDate")
	select *
	from kline
	#set(flag=0)
	#if(date)
		#(flag==0?"where":"and") date >= #para(date)
		#set(flag=1)
	#end
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
		#set(flag=1)
	#end
	order by date asc
#end

#sql("getByDateRange")
	select *
	from kline
	#set(flag=0)
	#if(startDate)
		#(flag==0?"where":"and") date >= #para(startDate)
		#set(flag=1)
	#end
	#if(endDate)
		#(flag==0?"where":"and") date <= #para(endDate)
		#set(flag=1)
	#end
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
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
	select *
	from kline
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") date <= #para(date)
		#set(flag=1)
	#end
	order by date desc limit #para(limit)
#end
#sql("deleteByCurrencyId")
    delete from kline where currencyId = #para(currencyId)
#end
#sql("getByDate")
	select *
	from kline
	#set(flag=0)
	#if(currencyId)
		#(flag==0?"where":"and") currencyId = #para(currencyId)
		#set(flag=1)
	#end
	#if(type)
		#(flag==0?"where":"and") type = #para(type)
		#set(flag=1)
	#end
	#if(date)
		#(flag==0?"where":"and") date = #para(date)
		#set(flag=1)
	#end
#end
