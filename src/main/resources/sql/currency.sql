#sql("paginate")
	select
	c.*,
	max(cs.id) as csid
	from currency c left join currency_strategy cs on cs.currencyId=c.id
	#set(flag=0)
	#if(qo.keyword)
		#(flag==0?"where":"and") (name like concat("%",#para(qo.keyword),"%") or code like concat("%",#para(qo.keyword),"%"))
		#set(flag=1)
	#end
	#if(qo.followed)
		#(flag==0?"where":"and") followed=1
		#set(flag=1)
	#end
	group by c.id
#end

#sql("paginateToBuy")
	select c.*,cs.id as csId,DATE_FORMAT(cs.startDate,'%Y-%m-%d') as startDate,s.`describe` as `describe`,cs.cutLine as cutLine
	from currency c JOIN currency_strategy cs
	on cs.currencyId=c.id join strategy s on cs.strategyId=s.id
	#set(flag=0)
	#if(qo.keyword)
		#(flag==0?"where":"and") (c.name like concat("%",#para(qo.keyword),"%") or c.code like concat("%",#para(qo.keyword),"%"))
		#set(flag=1)
	#end
	#if(qo.cutLine)
		#(flag==0?"where":"and") cs.cutLine is not null
		#set(flag=1)
	#end
	#if(qo.followed)
		#(flag==0?"where":"and") c.followed=1
		#set(flag=1)
	#end
	#if(qo.strategyId)
		#(flag==0?"where":"and") cs.strategyId=#para(qo.strategyId)
		#set(flag=1)
	#end
	order by cs.startDate desc
#end

#sql("findByCode")
	select *
	from currency
	#set(flag=0)
	#if(code)
		#(flag==0?"where":"and") code = #para(code)
		#set(flag=1)
	#end
#end

#sql("listAll")
	select * from currency
#end

#sql("listAllByCutLine")
	select * from currency where cutLine != null
#end

#sql("listForStrategy")
	select * from currency c join currency_strategy cs on cs.currencyId=c.id
#end

#sql("listFollowed")
	select * from currency c where followed = 1
#end
