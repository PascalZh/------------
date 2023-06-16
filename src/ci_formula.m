seg_node_table = [1,1,2;2,2,5;3,2,3;4,3,4;5,4,5;6,5,6;7,6,7;8,7,8;9,7,9;10,8,10;11,9,11;12,11,12;13,12,13;14,13,14;15,14,15;16,13,16;17,15,17;18,16,17;19,17,18;20,18,32;21,16,19;22,14,20;23,20,21;24,21,22;25,20,22;26,24,23;27,15,24;28,23,25;29,25,26;30,25,31;31,31,27;32,27,29;34,29,28;35,22,33;36,33,34;37,32,19;38,29,35;39,35,30;40,28,35;41,28,36];
c0 = sym('c0', [36, 1]);
c = sym('c', [36, 1]);
k = sym('k', [36, 36]);
t = sym('t', [36, 36]);
Q = sym('Q', [36, 1]);

iterations = 0;
while 1
    iterations = iterations + 1
    hasAllExpanded = 1;
    for i = 1:36
        idx = find(seg_node_table(:, 3) == i);
        if has(c(i), ['c' num2str(i)])
            if isempty(idx)
                c(i) = c0(i);
            else
                c(i) = sym(0);
                sum_Q = sym(0);
                for m = 1:length(idx)
                    j = seg_node_table(idx(m), 2);
                    sum_Q = sum_Q + Q(j);
                    c(i) = c(i) + c(j)*exp(-k(j, i)*t(j, i))*Q(j);
                    if has(c(i), ['c' num2str(j)])
                        hasAllExpanded = 0;
                    end
                end
                c(i) = c(i) / sum_Q;
                c(i) = c(i) + c0(i);
            end
        else
            for j = 1:36
                if has(c(i), ['c' num2str(j)])
                    hasAllExpanded = 0;
                    c(i) = subs(c(i), ['c' num2str(j)], c(j));
                end
            end
        end
    end
    if hasAllExpanded == 1
        break;
    end
end
c