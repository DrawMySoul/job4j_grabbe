--select the names of all persons who are not members of the company with id = 5
select person.name as emploee, company.name as company
from person join company on person.company_id = company.id 
where company.id !=5;

--select the name of the companies with the maximum number of employees
select company.name as company, count(person.*) as amount 
from person join company on person.company_id = company.id
group by company.name
having count(person.*) = (select count(person.*) as c 
						  from person 
						  group by person.company_id
						  order by c desc
						  limit 1)