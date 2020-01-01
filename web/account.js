function select(button)
{
	$.ajax
	({
		type:'GET',
		dataType:'text',
		url:'./accountadmin',
		data:'operation=select&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')');
			var resultForm='';
			data.forEach(function(result)
			{
				resultForm+="<form class='tr'>";
				for(column in columns)
				{
					var columnInfo=columns[column];
					var key=columnInfo.key;
					if(key=='')
						continue;
					var value=result[key]!=null?result[key]:'';
					if(columnInfo.type=='view')
						resultForm+="<input class='td' type='text' value='"+value+"' disabled>";
					else if(columnInfo.type=='password')
						resultForm+=
							"<input type='hidden' name='"+"p"+key+"' value='"+value+"'>\
							<input class='td' type='password' name='"+key+"' value='"+value+"' list='"+key+"' oninput='updateable($(this))'>";
					else
						resultForm+=
							"<input type='hidden' name='"+"p"+key+"' value='"+value+"'>\
							<input class='td' type='text' name='"+key+"' value='"+value+"' list='"+key+"' oninput='updateable($(this))'>";
				}
				resultForm+=
					"<div class='td tr'>\
					<div class='td tb'>\
					<button class='update' type='button' onclick='update($(this));' disabled>Update</button>\
					</div>\
					<div class='td tb'>\
					<button class='delete' type='button' onclick='del($(this));'>Delete</button>\
					</div>\
					</div>\
					</form>";
			});
			switch(button.attr('class'))
			{
				case 'insert':
					$('div#showTable').prepend(resultForm);
					break;
				case 'update':
					button.parents('form').replaceWith(resultForm);
					break;
			}
			if(data.rows==0)
				alert('None records matched!');
			$(window).resize();
		},
		error:function(){errorTip('Offline!');}
	});
}

function insr(button)
{
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./accountadmin',
		data:'operation=insert&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
				select(button);
			else
				errorTip('Error!');
		},
		error:function(){errorTip('Offline!');}
	});
}

function update(button)
{
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./accountadmin',
		data:'operation=update&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
				select(button);
			else
				errorTip('Error!');
		},
		error:function(){errorTip('Offline!');}
	});
}

function del(button)
{
	$.ajax
	({
		type:'POST',
		dataType:'text',
		url:'./accountadmin',
		data:'operation=delete&'+button.parents('form').serialize(),
		success:function(data)
		{
			data=eval('('+data+')')[0];
			if(data)
				button.parents('form').remove();
			else
				errorTip('Error!');
		},
		error:function(){errorTip('Offline!');}
	});
}

function errorTip(message)
{
	var tip='Error!\n'+message;
	if(message=='Offline!')
		tip+='\nPlease check your network connection and try again!';
	else
		tip+='\nPlease check your parameters and try again!';
	alert(tip);
}

function insertable(input)
{
	var flag=true;
	var form=input.parents('form');
	for(column in columns)
	{
		var columnInfo=columns[column];
		switch(columnInfo.type)
		{
			case 'primary':
				if(form.find("input:text[name='"+columnInfo.key+"']").val()=='')
					flag=false;
				break;
			case 'view':
				if(form.find("input:text[name='"+columnInfo.key+"']").val()!='')
					flag=false;
				break;
		}
		if(!flag)
			break;
	}
	if(flag)
		$('button.insert').removeAttr('disabled');
	else
		$('button.insert').attr('disabled',true);
}

function updateable(input)
{
	var flag=false;
	var form=input.parents('form');
	for(column in columns)
	{
		var columnInfo=columns[column];
		var key=columnInfo.key;
		if(key=='')
			continue;
		if(form.find("input[name='"+key+"']").val()!=form.find("input[name='p"+key+"']").val())
			flag=true;
		if(columnInfo.type=='primary'&&form.find("input[name='"+key+"']").val()=='')
		{
			flag=false;
			break;
		}
	}
	if(flag)
		form.find('button.update').removeAttr('disabled');
	else
		form.find('button.update').attr('disabled',true);
}

$(window).resize(function()
{
	$('div#blankTable').css('height',$('div#headTable').css('height'));
});
var columns=
{
	'Username':{'key':'username','type':'primary','foreign':false},
	'Password':{'key':'password','type':'password','foreign':false},
	'SMTP Server':{'key':'smtp','type':'field','foreign':false},
	'POP3 Server':{'key':'pop3','type':'field','foreign':false},
	'Operation':{'key':'','type':'','foreign':false}
};
var headline='';
for(column in columns)
{
	var columnInfo=columns[column]; 
	headline+="<span class='td'>";
	headline+=column;
	headline+="</span>";
}
$('div#headline').html(headline);
var selectForm="<form class='tr'>";
for(column in columns)
{
	var key=columns[column].key;
	if(key=='')
		continue;
	selectForm+="<input class='td' type='text' name='"+key+"' value='' list='"+key+"' oninput='insertable($(this));'>";
}
selectForm+=
	"<div class='td tr'>\
	<div class='td tb'>\
	<button id='insert' class='insert' type='button' onclick='insr($(this));' disabled>Insert</button>\
	</div>\
	<div class='td tb'>\
	</div>\
	</div>\
	</form>";
$('hr').before(selectForm);
select($('#insert'));
