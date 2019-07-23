create table quote (
    quote_id CHAR(16) FOR BIT DATA not null, 
    created timestamp not null, 
    text varchar(4096) not null, 
    source_id CHAR(16) FOR BIT DATA not null, 
    primary key (quote_id)
);

create table source (
    source_id CHAR(16) FOR BIT DATA not null, 
    created timestamp not null, 
    name varchar(1024) not null, 
    primary key (source_id)
);

alter table source add constraint UK_4a1uurs8rtj4xnah2j9uguec0 unique (name);

alter table quote add constraint FK4gnwxqrpbw5culhb0cxc6lnv0 foreign key (source_id) references source;
